package com.example.myapplication.data.repository

import android.net.Uri
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.CartLineFirestore
import com.example.myapplication.data.model.CatalogProductSummary
import com.example.myapplication.data.model.Product
import com.example.myapplication.data.model.ProductDetailBundle
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.model.ProductVariant
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.displayImagesForVariant
import com.example.myapplication.data.model.ui.CartLineUi
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(
    private val auth: FirebaseAuth = FirebaseRemoteDataSource.auth,
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val storage: FirebaseStorage = FirebaseRemoteDataSource.storage,
) {

    sealed class ImageUploadTarget {
        data object Public : ImageUploadTarget()
        data class Variant(val localVariantId: String) : ImageUploadTarget()
    }

    suspend fun uploadProductImage(localUri: Uri, target: ImageUploadTarget): Result<String> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("Store owner oturumu bulunamadi.")
        val store = getStoreByOwner(ownerId)
        val segment = when (target) {
            is ImageUploadTarget.Public -> "public"
            is ImageUploadTarget.Variant -> "variant/${target.localVariantId}"
        }
        val objectPath = "stores/${store.storeId}/product_uploads/$segment/${UUID.randomUUID()}"
        val ref = storage.reference.child(objectPath)
        ref.putFile(localUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun fetchCategories(): Result<List<Category>> = runCatching {
        db.collection(COLLECTION_CATEGORIES)
            .get()
            .await()
            .documents
            .map { it.toCategory() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun fetchBrands(): Result<List<Brand>> = runCatching {
        db.collection(COLLECTION_BRANDS)
            .get()
            .await()
            .documents
            .map { it.toBrand() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun fetchCatalogSummaries(): Result<List<CatalogProductSummary>> = runCatching {
        val docs = db.collection(COLLECTION_PRODUCTS).get().await().documents
        coroutineScope {
            docs.map { doc ->
                async {
                    val p = doc.toProduct()
                    val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                        .map { it.toProductVariant() }
                    val minPrice = variants.minOfOrNull { it.price } ?: 0.0
                    val imageUrl = p.publicImages.firstOrNull()
                        ?: variants.flatMap { it.images }.firstOrNull()
                        ?: ""
                    CatalogProductSummary(
                        productId = p.productId,
                        name = p.name,
                        categoryName = p.category["name"].takeIf { !it.isNullOrBlank() },
                        brandName = p.brand["name"].takeIf { !it.isNullOrBlank() },
                        rating = p.rating,
                        reviewCount = p.reviewCount,
                        imageUrl = imageUrl,
                        minPrice = minPrice,
                    )
                }
            }.awaitAll()
        }
    }

    /** [orderedProductIds] sirasi korunur (favori listesi gibi). whereIn limiti icin parcalanir. */
    suspend fun fetchCatalogSummariesForProductIds(orderedProductIds: List<String>): Result<List<CatalogProductSummary>> = runCatching {
        val orderedUnique = orderedProductIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (orderedUnique.isEmpty()) return@runCatching emptyList()
        val byId = LinkedHashMap<String, CatalogProductSummary>()
        orderedUnique.chunked(30).forEach { chunk ->
            val snaps = db.collection(COLLECTION_PRODUCTS)
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()
                .documents
            coroutineScope {
                snaps.map { doc ->
                    async {
                        val p = doc.toProduct()
                        val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                            .map { it.toProductVariant() }
                        val minPrice = variants.minOfOrNull { it.price } ?: 0.0
                        val imageUrl = p.publicImages.firstOrNull()
                            ?: variants.flatMap { it.images }.firstOrNull()
                            ?: ""
                        doc.id to CatalogProductSummary(
                            productId = p.productId,
                            name = p.name,
                            categoryName = p.category["name"].takeIf { !it.isNullOrBlank() },
                            brandName = p.brand["name"].takeIf { !it.isNullOrBlank() },
                            rating = p.rating,
                            reviewCount = p.reviewCount,
                            imageUrl = imageUrl,
                            minPrice = minPrice,
                        )
                    }
                }.awaitAll().forEach { (id, summary) -> byId[id] = summary }
            }
        }
        orderedUnique.mapNotNull { id -> byId[id] }
    }

    suspend fun enrichCartLines(lines: List<CartLineFirestore>): Result<List<CartLineUi>> = runCatching {
        val valid = lines.filter { it.quantity > 0 && it.productId.isNotBlank() && it.variantId.isNotBlank() }
        if (valid.isEmpty()) return@runCatching emptyList()
        coroutineScope {
            valid.map { line ->
                async {
                    val productSnap = db.collection(COLLECTION_PRODUCTS).document(line.productId).get().await()
                    if (!productSnap.exists()) return@async null
                    val vSnap = productSnap.reference.collection(SUBCOLLECTION_VARIANTS).document(line.variantId).get().await()
                    if (!vSnap.exists()) return@async null
                    val p = productSnap.toProduct()
                    val v = vSnap.toProductVariant()
                    val img = displayImagesForVariant(p, v).firstOrNull().orEmpty()
                    CartLineUi(
                        productId = line.productId,
                        variantId = line.variantId,
                        quantity = line.quantity,
                        productName = p.name,
                        brandName = p.brand["name"]?.takeIf { !it.isNullOrBlank() },
                        unitPrice = v.price,
                        imageUrl = img,
                        attributes = v.attributes,
                        sku = v.sku,
                        maxStock = v.stock,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    suspend fun fetchProductDetail(productId: String): Result<ProductDetailBundle> = runCatching {
        val snap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        if (!snap.exists()) error("Urun bulunamadi.")
        val product = snap.toProduct()
        val variants = snap.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
            .map { it.toProductVariant() }
        val categoryId = product.category["categoryId"].orEmpty()
        val keysFromCategory = if (categoryId.isNotBlank()) {
            val catSnap = db.collection(COLLECTION_CATEGORIES).document(categoryId).get().await()
            if (catSnap.exists()) catSnap.toCategory().variantAttributes else emptyList()
        } else {
            emptyList()
        }
        val keys = if (keysFromCategory.isNotEmpty()) {
            keysFromCategory
        } else {
            variants.flatMap { it.attributes.keys }.distinct().sorted()
        }
        ProductDetailBundle(product, keys, variants)
    }

    suspend fun createProductForCurrentOwner(input: CreateProductInput): Result<String> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("Store owner oturumu bulunamadi.")
        val store = getStoreByOwner(ownerId)

        val productRef = db.collection(COLLECTION_PRODUCTS).document()
        val now = System.currentTimeMillis()
        val product = Product(
            productId = productRef.id,
            storeId = store.storeId,
            name = input.name.trim(),
            description = input.description.trim(),
            publicImages = input.publicImages,
            brand = mapOf("brandId" to input.brandId, "name" to input.brandName),
            category = mapOf("categoryId" to input.categoryId, "name" to input.categoryName),
            rating = 0.0,
            reviewCount = 0,
            createdAt = now,
        )

        productRef.set(product.toMap()).await()

        val batch = db.batch()
        input.variants.forEach { draft ->
            val variantRef = productRef.collection(SUBCOLLECTION_VARIANTS).document()
            val variant = ProductVariant(
                variantId = variantRef.id,
                sku = draft.sku.trim(),
                attributes = draft.attributes,
                price = draft.price,
                stock = draft.stock,
                images = draft.images,
            )
            batch.set(variantRef, variant.toMap())
        }
        batch.commit().await()

        productRef.id
    }

    private suspend fun getStoreByOwner(ownerId: String): Store {
        val snap = db.collection(COLLECTION_STORES)
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull()
            ?: error("Bu store owner i?in store bulunamadi. stores koleksiyonuna ownerId ekleyin.")

        return doc.toStore()
    }

    private fun DocumentSnapshot.toBrand(): Brand =
        Brand(
            brandId = getString("brandId")?.takeIf { it.isNotBlank() } ?: id,
            name = getString("name").orEmpty(),
        )

    private fun DocumentSnapshot.toCategory(): Category {
        val rawAttributes = get("variantAttributes") as? List<*>
        val attrs = rawAttributes?.mapNotNull { it?.toString()?.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        return Category(
            categoryId = id,
            name = getString("name").orEmpty(),
            variantAttributes = attrs,
        )
    }

    private fun DocumentSnapshot.toStore(): Store =
        Store(
            storeId = id,
            ownerId = getString("ownerId").orEmpty(),
            name = getString("name").orEmpty(),
            description = getString("description").orEmpty(),
            logo = getString("logo").orEmpty(),
            rating = (getDouble("rating") ?: 0.0),
            createdAt = readMillis("createdAt"),
        )

    private fun DocumentSnapshot.toProduct(): Product {
        val brandMap = get("brand") as? Map<*, *> ?: emptyMap<Any, Any>()
        val brand = mapOf(
            "brandId" to (brandMap["brandId"]?.toString().orEmpty()),
            "name" to (brandMap["name"]?.toString().orEmpty()),
        )
        val catMap = get("category") as? Map<*, *> ?: emptyMap<Any, Any>()
        val category = mapOf(
            "categoryId" to (catMap["categoryId"]?.toString().orEmpty()),
            "name" to (catMap["name"]?.toString().orEmpty()),
        )
        val imgs = (get("publicImages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        return Product(
            productId = id,
            storeId = getString("storeId").orEmpty(),
            name = getString("name").orEmpty(),
            description = getString("description").orEmpty(),
            publicImages = imgs,
            brand = brand,
            category = category,
            rating = getDouble("rating") ?: 0.0,
            reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
            createdAt = readMillis("createdAt"),
        )
    }

    private fun DocumentSnapshot.toProductVariant(): ProductVariant {
        val attrsRaw = get("attributes") as? Map<*, *> ?: emptyMap<Any, Any>()
        val attrs = attrsRaw.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
        val imgs = (get("images") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        return ProductVariant(
            variantId = id,
            sku = getString("sku").orEmpty(),
            attributes = attrs,
            price = getDouble("price") ?: 0.0,
            stock = (getLong("stock") ?: 0L).toInt(),
            images = imgs,
        )
    }

    private fun Product.toMap(): Map<String, Any> =
        mapOf(
            "productId" to productId,
            "storeId" to storeId,
            "name" to name,
            "description" to description,
            "publicImages" to publicImages,
            "brand" to brand,
            "category" to category,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "createdAt" to createdAt,
        )

    private fun ProductVariant.toMap(): Map<String, Any> =
        mapOf(
            "variantId" to variantId,
            "sku" to sku,
            "attributes" to attributes,
            "price" to price,
            "stock" to stock,
            "images" to images,
        )

    data class VariantDraft(
        val sku: String,
        val attributes: Map<String, String>,
        val price: Double,
        val stock: Int,
        val images: List<String>,
    )

    data class CreateProductInput(
        val name: String,
        val description: String,
        val brandId: String,
        val brandName: String,
        val categoryId: String,
        val categoryName: String,
        val publicImages: List<String>,
        val variants: List<VariantDraft>,
    )

    companion object {
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_CATEGORIES = "categories"
        private const val COLLECTION_BRANDS = "brands"
        private const val SUBCOLLECTION_VARIANTS = "variants"
    }
}
