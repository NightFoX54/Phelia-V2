package com.example.myapplication.data.repository

import android.net.Uri
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.CartLineFirestore
import com.example.myapplication.data.model.CatalogProductSummary
import com.example.myapplication.data.model.InactiveProductAdminItem
import com.example.myapplication.data.model.Product
import com.example.myapplication.data.model.ProductDetailBundle
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.model.ProductVariant
import com.example.myapplication.data.model.finalPrice
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.data.model.displayImagesForVariant
import com.example.myapplication.data.model.ui.CartLineUi
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
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
    val auth: FirebaseAuth = FirebaseRemoteDataSource.auth,
    val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    val storage: FirebaseStorage = FirebaseRemoteDataSource.storage,
    val storeRepository: StoreRepository = StoreRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
) {

    sealed class ImageUploadTarget {
        data object Public : ImageUploadTarget()
        data class Variant(val localVariantId: String) : ImageUploadTarget()
    }

    suspend fun uploadProductImage(localUri: Uri, target: ImageUploadTarget): Result<String> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("No store owner session.")
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

    /** First image URL for a line item (variant images, then public). */
    suspend fun fetchLineItemDisplayImageUrl(productId: String, variantId: String): String? = runCatching {
        if (productId.isBlank()) return@runCatching null
        val pSnap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        if (!pSnap.exists()) return@runCatching null
        val p = pSnap.toProduct()
        val v = if (variantId.isNotBlank()) {
            val vSnap = pSnap.reference.collection(SUBCOLLECTION_VARIANTS).document(variantId).get().await()
            if (vSnap.exists()) vSnap.toProductVariant() else null
        } else {
            null
        }
        displayImagesForVariant(p, v).firstOrNull()
    }.getOrNull()

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
                    if (!p.isActive) return@async null
                    val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                        .map { it.toProductVariant() }
                    val active = variants.filter { it.isActive }
                    if (active.isEmpty()) return@async null
                    val minVariant = active.minByOrNull { it.finalPrice() } ?: return@async null
                    val minPrice = minVariant.finalPrice()
                    val minBasePrice = minVariant.price
                    val minDiscountPercent = minVariant.discountPercent.coerceIn(0, 100)
                    val imageUrl = p.publicImages.firstOrNull()
                        ?: active.flatMap { it.images }.firstOrNull()
                        ?: ""
                    val (reviewAvg, reviewCnt) = doc.reference.fetchReviewStatsFromSubcollection()
                    CatalogProductSummary(
                        productId = p.productId,
                        name = p.name,
                        categoryName = p.category["name"].takeIf { !it.isNullOrBlank() },
                        brandName = p.brand["name"].takeIf { !it.isNullOrBlank() },
                        rating = reviewAvg,
                        reviewCount = reviewCnt,
                        imageUrl = imageUrl,
                        minPrice = minPrice,
                        minBasePrice = minBasePrice,
                        minDiscountPercent = minDiscountPercent,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    /** Active catalog items sold by this store (customer-facing). */
    suspend fun fetchCatalogSummariesForStore(storeId: String): Result<List<CatalogProductSummary>> = runCatching {
        if (storeId.isBlank()) return@runCatching emptyList()
        val docs = db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(FIELD_PRODUCT_STORE_ID, storeId)
            .get()
            .await()
            .documents
        coroutineScope {
            docs.map { doc ->
                async {
                    val p = doc.toProduct()
                    if (!p.isActive) return@async null
                    val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                        .map { it.toProductVariant() }
                    val active = variants.filter { it.isActive }
                    if (active.isEmpty()) return@async null
                    val minVariant = active.minByOrNull { it.finalPrice() } ?: return@async null
                    val minPrice = minVariant.finalPrice()
                    val minBasePrice = minVariant.price
                    val minDiscountPercent = minVariant.discountPercent.coerceIn(0, 100)
                    val imageUrl = p.publicImages.firstOrNull()
                        ?: active.flatMap { it.images }.firstOrNull()
                        ?: ""
                    val (reviewAvg, reviewCnt) = doc.reference.fetchReviewStatsFromSubcollection()
                    CatalogProductSummary(
                        productId = p.productId,
                        name = p.name,
                        categoryName = p.category["name"].takeIf { !it.isNullOrBlank() },
                        brandName = p.brand["name"].takeIf { !it.isNullOrBlank() },
                        rating = reviewAvg,
                        reviewCount = reviewCnt,
                        imageUrl = imageUrl,
                        minPrice = minPrice,
                        minBasePrice = minBasePrice,
                        minDiscountPercent = minDiscountPercent,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    /** Preserves [orderedProductIds] order (e.g. favorites). Chunked for `whereIn` limits. */
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
                        if (!p.isActive) return@async null
                        val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                            .map { it.toProductVariant() }
                        val active = variants.filter { it.isActive }
                        if (active.isEmpty()) return@async null
                        val minVariant = active.minByOrNull { it.finalPrice() } ?: return@async null
                        val minPrice = minVariant.finalPrice()
                        val minBasePrice = minVariant.price
                        val minDiscountPercent = minVariant.discountPercent.coerceIn(0, 100)
                        val imageUrl = p.publicImages.firstOrNull()
                            ?: active.flatMap { it.images }.firstOrNull()
                            ?: ""
                        val (reviewAvg, reviewCnt) = doc.reference.fetchReviewStatsFromSubcollection()
                        doc.id to CatalogProductSummary(
                            productId = p.productId,
                            name = p.name,
                            categoryName = p.category["name"].takeIf { !it.isNullOrBlank() },
                            brandName = p.brand["name"].takeIf { !it.isNullOrBlank() },
                            rating = reviewAvg,
                            reviewCount = reviewCnt,
                            imageUrl = imageUrl,
                            minPrice = minPrice,
                            minBasePrice = minBasePrice,
                            minDiscountPercent = minDiscountPercent,
                        )
                    }
                }.awaitAll().filterNotNull().forEach { (id, summary) -> byId[id] = summary }
            }
        }
        orderedUnique.mapNotNull { id -> byId[id] }
    }

    suspend fun enrichCartLines(lines: List<CartLineFirestore>): Result<List<CartLineUi>> = runCatching {
        val valid = lines.filter { it.quantity > 0 && it.productId.isNotBlank() && it.variantId.isNotBlank() }
        if (valid.isEmpty()) return@runCatching emptyList()
        val categoryTaxCache = mutableMapOf<String, Int>()
        coroutineScope {
            valid.map { line ->
                async {
                    val productSnap = db.collection(COLLECTION_PRODUCTS).document(line.productId).get().await()
                    if (!productSnap.exists()) return@async null
                    val vSnap = productSnap.reference.collection(SUBCOLLECTION_VARIANTS).document(line.variantId).get().await()
                    if (!vSnap.exists()) return@async null
                    val p = productSnap.toProduct()
                    val v = vSnap.toProductVariant()
                    if (!p.isActive || !v.isActive) return@async null
                    val img = displayImagesForVariant(p, v).firstOrNull().orEmpty()
                    val categoryId = p.category["categoryId"].orEmpty()
                    val taxRatePercent = if (categoryId.isBlank()) {
                        0
                    } else {
                        categoryTaxCache[categoryId] ?: run {
                            val catSnap = db.collection(COLLECTION_CATEGORIES).document(categoryId).get().await()
                            val rate = (catSnap.getLong("taxRate") ?: 0L).toInt().coerceAtLeast(0)
                            categoryTaxCache[categoryId] = rate
                            rate
                        }
                    }
                    CartLineUi(
                        productId = line.productId,
                        storeId = p.storeId,
                        categoryId = categoryId,
                        variantId = line.variantId,
                        quantity = line.quantity,
                        productName = p.name,
                        brandName = p.brand["name"]?.takeIf { !it.isNullOrBlank() },
                        unitPrice = v.finalPrice(),
                        baseUnitPrice = v.price,
                        discountPercent = v.discountPercent.coerceIn(0, 100),
                        taxRatePercent = taxRatePercent,
                        imageUrl = img,
                        attributes = v.attributes,
                        sku = v.sku,
                        maxStock = v.stock,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    suspend fun fetchStoreOwnerProductRows(storeId: String): Result<List<StoreOwnerProductRow>> = runCatching {
        if (storeId.isBlank()) return@runCatching emptyList()
        val docs = db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(FIELD_PRODUCT_STORE_ID, storeId)
            .get()
            .await()
            .documents
        coroutineScope {
            docs.map { doc ->
                async {
                    val p = doc.toProduct()
                    val variants = doc.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
                        .map { it.toProductVariant() }
                    val active = variants.filter { it.isActive }
                    val minPrice = active.minOfOrNull { it.finalPrice() } ?: 0.0
                    val totalStock = active.sumOf { it.stock }
                    val img = p.publicImages.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?: active.flatMap { it.images }.firstOrNull { it.isNotBlank() }.orEmpty()
                    val (_, reviewCnt) = doc.reference.fetchReviewStatsFromSubcollection()
                    StoreOwnerProductRow(
                        productId = p.productId,
                        name = p.name,
                        categoryName = p.category["name"]?.takeIf { it.isNotBlank() } ?: "—",
                        imageUrl = img,
                        minPrice = minPrice,
                        totalStock = totalStock,
                        variantCount = variants.size,
                        reviewCount = reviewCnt,
                        isActive = p.isActive,
                    )
                }
            }.awaitAll()
        }.sortedBy { it.name.lowercase() }
    }

    /**
     * @param forStoreManagement when true, inactive products/variants are included (store owner editing).
     */
    suspend fun fetchProductDetail(
        productId: String,
        forStoreManagement: Boolean = false,
    ): Result<ProductDetailBundle> = runCatching {
        val snap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        if (!snap.exists()) error("Product not found.")
        val product = snap.toProduct()
        if (!forStoreManagement && !product.isActive) {
            error("This product is no longer for sale.")
        }
        val allVariants = snap.reference.collection(SUBCOLLECTION_VARIANTS).get().await().documents
            .map { it.toProductVariant() }
        val variants = if (forStoreManagement) {
            allVariants
        } else {
            allVariants.filter { it.isActive }
        }
        if (!forStoreManagement && variants.isEmpty()) {
            error("No active variant for this product.")
        }
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
        val (aggRating, aggCount) = snap.reference.fetchReviewStatsFromSubcollection()
        val productForBundle =
            if (aggCount > 0) product.copy(rating = aggRating, reviewCount = aggCount) else product
        val store = product.storeId.takeIf { it.isNotBlank() }?.let { sid ->
            runCatching { storeRepository.fetchStoreById(sid) }.getOrNull()
        }
        ProductDetailBundle(productForBundle, keys, variants, store)
    }

    suspend fun createProductForCurrentOwner(input: CreateProductInput): Result<String> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("No store owner session.")
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
            isActive = true,
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
                discountPercent = draft.discountPercent,
                stock = draft.stock,
                images = draft.images,
                isActive = true,
            )
            batch.set(variantRef, variant.toMap())
        }
        batch.commit().await()

        productRef.id
    }

    data class VariantDraftPersisted(
        /** Firestore variant document id; null for a new variant row */
        val firestoreVariantId: String?,
        val sku: String,
        val attributes: Map<String, String>,
        val price: Double,
        val discountPercent: Int,
        val stock: Int,
        val images: List<String>,
    )

    data class UpdateProductInput(
        val name: String,
        val description: String,
        val brandId: String,
        val brandName: String,
        val categoryId: String,
        val categoryName: String,
        val publicImages: List<String>,
        val variants: List<VariantDraftPersisted>,
    )

    suspend fun updateProductForCurrentOwner(
        productId: String,
        input: UpdateProductInput,
    ): Result<Unit> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("No store owner session.")
        val store = getStoreByOwner(ownerId)
        val productRef = db.collection(COLLECTION_PRODUCTS).document(productId)
        val snap = productRef.get().await()
        if (!snap.exists()) error("Product not found.")
        val existing = snap.toProduct()
        if (existing.storeId != store.storeId) error("This product is not in your store.")

        productRef.update(
            mapOf(
                "name" to input.name.trim(),
                "description" to input.description.trim(),
                "publicImages" to input.publicImages,
                "brand" to mapOf("brandId" to input.brandId, "name" to input.brandName),
                "category" to mapOf("categoryId" to input.categoryId, "name" to input.categoryName),
            ),
        ).await()

        val variantsCol = productRef.collection(SUBCOLLECTION_VARIANTS)
        val existingIds = variantsCol.get().await().documents.map { it.id }.toSet()
        val existingPriceByVariantId = variantsCol.get().await().documents.associate { d ->
            d.id to ((d.getDouble("price") ?: 0.0))
        }
        val keepIds = input.variants.mapNotNull { it.firestoreVariantId?.takeIf { id -> id.isNotBlank() } }.toSet()

        val batch = db.batch()
        for (id in existingIds) {
            if (id !in keepIds) {
                batch.update(variantsCol.document(id), mapOf(FIELD_IS_ACTIVE to false))
            }
        }
        for (v in input.variants) {
            val fid = v.firestoreVariantId?.takeIf { it.isNotBlank() }
            if (fid != null && fid in existingIds) {
                val docRef = variantsCol.document(fid)
                val variant = ProductVariant(
                    variantId = fid,
                    sku = v.sku.trim(),
                    attributes = v.attributes,
                    price = v.price,
                    discountPercent = v.discountPercent,
                    stock = v.stock,
                    images = v.images,
                    isActive = true,
                )
                batch.set(docRef, variant.toMap())
            } else {
                val newRef = variantsCol.document()
                val variant = ProductVariant(
                    variantId = newRef.id,
                    sku = v.sku.trim(),
                    attributes = v.attributes,
                    price = v.price,
                    discountPercent = v.discountPercent,
                    stock = v.stock,
                    images = v.images,
                    isActive = true,
                )
                batch.set(newRef, variant.toMap())
            }
        }
        batch.commit().await()

        // Notify users who favorited this product when any existing variant price decreased.
        val lowered = input.variants.any { v ->
            val fid = v.firestoreVariantId?.takeIf { it.isNotBlank() } ?: return@any false
            val oldPrice = existingPriceByVariantId[fid] ?: return@any false
            v.price < oldPrice
        }
        if (lowered) {
            val favSnap = db.collectionGroup("favorites")
                .whereEqualTo("productId", productId)
                .get()
                .await()
            val targetUserIds = favSnap.documents.mapNotNull { doc ->
                doc.reference.parent.parent?.id
            }.distinct()
            notificationRepository.sendToUsers(
                userIds = targetUserIds,
                type = NotificationTypes.PRICE_DROP,
                title = "Price drop alert",
                body = "${input.name.trim().ifBlank { "A product" }} is now cheaper.",
                productId = productId,
            )
        }
    }

    suspend fun deactivateProductForCurrentOwner(productId: String): Result<Unit> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("No store owner session.")
        val store = getStoreByOwner(ownerId)
        val productRef = db.collection(COLLECTION_PRODUCTS).document(productId)
        val snap = productRef.get().await()
        if (!snap.exists()) error("Product not found.")
        val existing = snap.toProduct()
        if (existing.storeId != store.storeId) error("This product is not in your store.")
        val batch = db.batch()
        batch.update(productRef, mapOf(FIELD_IS_ACTIVE to false))
        productRef.collection(SUBCOLLECTION_VARIANTS).get().await().documents.forEach { vdoc ->
            batch.update(vdoc.reference, mapOf(FIELD_IS_ACTIVE to false))
        }
        batch.commit().await()
    }

    suspend fun activateProductForCurrentOwner(productId: String): Result<Unit> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("No store owner session.")
        val store = getStoreByOwner(ownerId)
        val productRef = db.collection(COLLECTION_PRODUCTS).document(productId)
        val snap = productRef.get().await()
        if (!snap.exists()) error("Product not found.")
        val existing = snap.toProduct()
        if (existing.storeId != store.storeId) error("This product is not in your store.")
        val batch = db.batch()
        batch.update(productRef, mapOf(FIELD_IS_ACTIVE to true))
        productRef.collection(SUBCOLLECTION_VARIANTS).get().await().documents.forEach { vdoc ->
            batch.update(vdoc.reference, mapOf(FIELD_IS_ACTIVE to true))
        }
        batch.commit().await()
    }

    /** Admin: products removed from sale (Firestore rules should enforce admin). */
    suspend fun fetchInactiveProductsForAdmin(): Result<List<InactiveProductAdminItem>> = runCatching {
        db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(FIELD_IS_ACTIVE, false)
            .get()
            .await()
            .documents
            .map { doc ->
                val p = doc.toProduct()
                InactiveProductAdminItem(
                    productId = p.productId,
                    name = p.name.ifBlank { p.productId },
                    storeId = p.storeId,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun adminReactivateProduct(productId: String): Result<Unit> = runCatching {
        if (productId.isBlank()) error("Product id is empty.")
        val productRef = db.collection(COLLECTION_PRODUCTS).document(productId)
        val snap = productRef.get().await()
        if (!snap.exists()) error("Product not found.")
        val batch = db.batch()
        batch.update(productRef, mapOf(FIELD_IS_ACTIVE to true))
        productRef.collection(SUBCOLLECTION_VARIANTS).get().await().documents.forEach { vdoc ->
            batch.update(vdoc.reference, mapOf(FIELD_IS_ACTIVE to true))
        }
        batch.commit().await()
    }

    private suspend fun getStoreByOwner(ownerId: String): Store {
        val snap = db.collection(COLLECTION_STORES)
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull()
            ?: error("No store found for this owner. Add ownerId on a document in stores.")

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
            taxRate = (getLong("taxRate") ?: 0L).toInt().coerceAtLeast(0),
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
            reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
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
            isActive = readIsActiveField(),
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
            discountPercent = (getLong("discountPercent") ?: 0L).toInt().coerceIn(0, 100),
            stock = (getLong("stock") ?: 0L).toInt(),
            images = imgs,
            isActive = readIsActiveField(),
        )
    }

    /** Average rating and count from `products/{id}/reviews` (product doc fields may be stale). */
    private suspend fun DocumentReference.fetchReviewStatsFromSubcollection(): Pair<Double, Int> {
        val docs = collection(SUBCOLLECTION_REVIEWS).get().await().documents
        if (docs.isEmpty()) return 0.0 to 0
        val ratings = docs.mapNotNull { d ->
            val r = d.getDouble(FIELD_REVIEW_RATING) ?: return@mapNotNull null
            if (r in 0.0..5.0) r else null
        }
        val n = docs.size
        val avg = if (ratings.isEmpty()) 0.0 else ratings.average()
        return avg to n
    }

    private fun DocumentSnapshot.readIsActiveField(): Boolean = getBoolean(FIELD_IS_ACTIVE) ?: true

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
            FIELD_IS_ACTIVE to isActive,
        )

    private fun ProductVariant.toMap(): Map<String, Any> =
        mapOf(
            "variantId" to variantId,
            "sku" to sku,
            "attributes" to attributes,
            "price" to price,
            "discountPercent" to discountPercent.coerceIn(0, 100),
            "stock" to stock,
            "images" to images,
            FIELD_IS_ACTIVE to isActive,
        )

    data class VariantDraft(
        val sku: String,
        val attributes: Map<String, String>,
        val price: Double,
        val discountPercent: Int,
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
        private const val FIELD_IS_ACTIVE = "isActive"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_PRODUCTS = "products"
        private const val FIELD_PRODUCT_STORE_ID = "storeId"
        private const val COLLECTION_CATEGORIES = "categories"
        private const val COLLECTION_BRANDS = "brands"
        private const val SUBCOLLECTION_VARIANTS = "variants"
        private const val SUBCOLLECTION_REVIEWS = "reviews"
        private const val FIELD_REVIEW_RATING = "rating"
    }
}
