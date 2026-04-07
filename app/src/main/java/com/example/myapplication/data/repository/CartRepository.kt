package com.example.myapplication.data.repository

import com.example.myapplication.data.model.CartLineFirestore
import com.example.myapplication.data.model.cartDocId
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class StockValidationResult(
    val warnings: List<String>,
)

class CartRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    fun listenCartLines(
        userId: String,
        onUpdate: (List<CartLineFirestore>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_USERS).document(userId).collection(SUBCOLLECTION_CART)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val productId = doc.getString(FIELD_PRODUCT_ID)?.trim().orEmpty()
                    val variantId = doc.getString(FIELD_VARIANT_ID)?.trim().orEmpty()
                    val qty = (doc.getLong(FIELD_QUANTITY) ?: 0L).toInt()
                    if (productId.isEmpty() || variantId.isEmpty() || qty <= 0) null
                    else CartLineFirestore(
                        documentId = doc.id,
                        productId = productId,
                        variantId = variantId,
                        quantity = qty,
                    )
                } ?: emptyList()
                onUpdate(list)
            }

    suspend fun addOrIncrement(userId: String, productId: String, variantId: String, delta: Int): Result<Unit> = runCatching {
        require(delta != 0) { "delta 0 olamaz" }
        val docId = cartDocId(productId, variantId)
        val ref = cartRef(userId).document(docId)
        val variantRef = db.collection(COLLECTION_PRODUCTS).document(productId)
            .collection(SUBCOLLECTION_VARIANTS).document(variantId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val current = if (snap.exists()) (snap.getLong(FIELD_QUANTITY) ?: 0L).toInt() else 0
            val vSnap = tx.get(variantRef)
            if (!vSnap.exists()) {
                if (snap.exists()) tx.delete(ref)
                return@runTransaction
            }
            val stock = (vSnap.getLong("stock") ?: 0L).toInt()
            if (stock <= 0) {
                if (snap.exists()) tx.delete(ref)
                return@runTransaction
            }
            var newQty = current + delta
            newQty = newQty.coerceIn(0, stock)
            if (newQty <= 0) {
                if (snap.exists()) tx.delete(ref)
            } else {
                tx.set(
                    ref,
                    mapOf(
                        FIELD_PRODUCT_ID to productId,
                        FIELD_VARIANT_ID to variantId,
                        FIELD_QUANTITY to newQty,
                    ),
                    SetOptions.merge(),
                )
            }
        }.await()
    }

    suspend fun setQuantity(userId: String, productId: String, variantId: String, quantity: Int): Result<Unit> = runCatching {
        val docId = cartDocId(productId, variantId)
        val ref = cartRef(userId).document(docId)
        if (quantity <= 0) {
            ref.delete().await()
            return@runCatching
        }
        val variantSnap = db.collection(COLLECTION_PRODUCTS).document(productId)
            .collection(SUBCOLLECTION_VARIANTS).document(variantId).get().await()
        val stock = if (variantSnap.exists()) (variantSnap.getLong("stock") ?: 0L).toInt() else 0
        val clamped = quantity.coerceIn(0, stock.coerceAtLeast(0))
        if (clamped <= 0) {
            ref.delete().await()
        } else {
            ref.set(
                mapOf(
                    FIELD_PRODUCT_ID to productId,
                    FIELD_VARIANT_ID to variantId,
                    FIELD_QUANTITY to clamped,
                ),
                SetOptions.merge(),
            ).await()
        }
    }

    suspend fun removeLine(userId: String, productId: String, variantId: String): Result<Unit> = runCatching {
        cartRef(userId).document(cartDocId(productId, variantId)).delete().await()
    }

    /**
     * Sepet acildiginda: stok 0 ise satiri siler, talep > stok ise miktari stoga indirir.
     */
    suspend fun validateAndAdjustStock(userId: String): Result<StockValidationResult> = runCatching {
        val warnings = mutableListOf<String>()
        val snap = cartRef(userId).get().await()
        if (snap.isEmpty) return@runCatching StockValidationResult(emptyList())

        val batch = db.batch()
        var hasWrites = false

        for (doc in snap.documents) {
            val productId = doc.getString(FIELD_PRODUCT_ID)?.trim().orEmpty()
            val variantId = doc.getString(FIELD_VARIANT_ID)?.trim().orEmpty()
            val qty = (doc.getLong(FIELD_QUANTITY) ?: 0L).toInt()
            if (productId.isEmpty() || variantId.isEmpty()) {
                batch.delete(doc.reference)
                hasWrites = true
                continue
            }

            val title = fetchProductTitle(productId)
            val variantRef = db.collection(COLLECTION_PRODUCTS).document(productId)
                .collection(SUBCOLLECTION_VARIANTS).document(variantId)
            val vSnap = variantRef.get().await()

            if (!vSnap.exists()) {
                batch.delete(doc.reference)
                hasWrites = true
                warnings.add("$title: Bu varyant artık mevcut değil; sepetten kaldırıldı.")
                continue
            }

            val stock = (vSnap.getLong("stock") ?: 0L).toInt()
            when {
                stock <= 0 -> {
                    batch.delete(doc.reference)
                    hasWrites = true
                    warnings.add("$title: Stokta kalmadığı için sepetten çıkarıldı.")
                }
                qty > stock -> {
                    batch.update(doc.reference, mapOf(FIELD_QUANTITY to stock))
                    hasWrites = true
                    warnings.add(
                        "$title: Sepetinizde $qty adet vardı; güncel stok $stock adet olduğu için adet $stock olarak güncellendi.",
                    )
                }
            }
        }

        if (hasWrites) batch.commit().await()
        StockValidationResult(warnings.distinct())
    }

    private suspend fun fetchProductTitle(productId: String): String {
        val snap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        return snap.getString("name")?.trim()?.takeIf { it.isNotEmpty() } ?: "Ürün"
    }

    private fun cartRef(userId: String) =
        db.collection(COLLECTION_USERS).document(userId).collection(SUBCOLLECTION_CART)

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_CART = "cartItems"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_VARIANT_ID = "variantId"
        private const val FIELD_QUANTITY = "quantity"
        private const val COLLECTION_PRODUCTS = "products"
        private const val SUBCOLLECTION_VARIANTS = "variants"
    }
}
