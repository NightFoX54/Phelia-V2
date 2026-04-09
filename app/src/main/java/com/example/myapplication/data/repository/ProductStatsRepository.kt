package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ProductStatsSnapshot
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await

/**
 * Product analytics: per-product subcollection [SUBCOLLECTION_STATS]/[STATS_DOC_ID] and denormalized
 * [COLLECTION_PRODUCT_STATS] (doc id = productId) for admin queries.
 *
 * Favorite count is **net** (+1 on add, −1 on remove, floored at 0) so toggle spam does not inflate totals.
 * [FIELD_ADDED_TO_CART] increases only when units are actually added (not when quantity is reduced).
 */
class ProductStatsRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    /** Use inside an existing [Transaction] (e.g. checkout). */
    fun applyPurchasedIncrement(
        tx: Transaction,
        productId: String,
        quantity: Int,
        productSnap: DocumentSnapshot,
    ) {
        if (productId.isBlank() || quantity <= 0 || !productSnap.exists()) return
        val (storeId, categoryId) = productSnap.storeIdAndCategoryId()
        val aggRef = db.collection(COLLECTION_PRODUCTS).document(productId)
            .collection(SUBCOLLECTION_STATS).document(STATS_DOC_ID)
        val globalRef = db.collection(COLLECTION_PRODUCT_STATS).document(productId)
        val q = quantity.toLong()
        tx.set(
            aggRef,
            mapOf(FIELD_PURCHASED to FieldValue.increment(q)),
            SetOptions.merge(),
        )
        tx.set(
            globalRef,
            mapOf(
                FIELD_PURCHASED to FieldValue.increment(q),
                FIELD_PRODUCT_ID to productId,
                FIELD_STORE_ID to storeId,
                FIELD_CATEGORY_ID to categoryId,
            ),
            SetOptions.merge(),
        )
    }

    suspend fun recordProductView(productId: String) {
        if (productId.isBlank()) return
        runCatching {
            val pSnap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
            if (!pSnap.exists()) return
            val (storeId, categoryId) = pSnap.storeIdAndCategoryId()
            val batch = db.batch()
            val aggRef = db.collection(COLLECTION_PRODUCTS).document(productId)
                .collection(SUBCOLLECTION_STATS).document(STATS_DOC_ID)
            val globalRef = db.collection(COLLECTION_PRODUCT_STATS).document(productId)
            batch.set(
                aggRef,
                mapOf(FIELD_VIEWS to FieldValue.increment(1)),
                SetOptions.merge(),
            )
            batch.set(
                globalRef,
                mapOf(
                    FIELD_VIEWS to FieldValue.increment(1),
                    FIELD_PRODUCT_ID to productId,
                    FIELD_STORE_ID to storeId,
                    FIELD_CATEGORY_ID to categoryId,
                ),
                SetOptions.merge(),
            )
            batch.commit().await()
        }
    }

    suspend fun recordAddToCart(productId: String, unitsAdded: Int) {
        if (productId.isBlank() || unitsAdded <= 0) return
        runCatching {
            val pSnap = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
            if (!pSnap.exists()) return
            val (storeId, categoryId) = pSnap.storeIdAndCategoryId()
            val batch = db.batch()
            val u = unitsAdded.toLong()
            val aggRef = db.collection(COLLECTION_PRODUCTS).document(productId)
                .collection(SUBCOLLECTION_STATS).document(STATS_DOC_ID)
            val globalRef = db.collection(COLLECTION_PRODUCT_STATS).document(productId)
            batch.set(
                aggRef,
                mapOf(FIELD_ADDED_TO_CART to FieldValue.increment(u)),
                SetOptions.merge(),
            )
            batch.set(
                globalRef,
                mapOf(
                    FIELD_ADDED_TO_CART to FieldValue.increment(u),
                    FIELD_PRODUCT_ID to productId,
                    FIELD_STORE_ID to storeId,
                    FIELD_CATEGORY_ID to categoryId,
                ),
                SetOptions.merge(),
            )
            batch.commit().await()
        }
    }

    /**
     * @param delta +1 when user favorites, −1 when user unfavorites.
     */
    suspend fun adjustFavoriteCount(productId: String, delta: Int) {
        if (productId.isBlank() || delta == 0) return
        runCatching {
            db.runTransaction { tx ->
                val pSnap = tx.get(db.collection(COLLECTION_PRODUCTS).document(productId))
                if (!pSnap.exists()) return@runTransaction null
                val (storeId, categoryId) = pSnap.storeIdAndCategoryId()
                val aggRef = db.collection(COLLECTION_PRODUCTS).document(productId)
                    .collection(SUBCOLLECTION_STATS).document(STATS_DOC_ID)
                val globalRef = db.collection(COLLECTION_PRODUCT_STATS).document(productId)
                val aggSnap = tx.get(aggRef)
                val globalSnap = tx.get(globalRef)
                val curAgg = (aggSnap.getLong(FIELD_ADDED_TO_FAVORITE) ?: 0L)
                val curGlobal = (globalSnap.getLong(FIELD_ADDED_TO_FAVORITE) ?: 0L)
                val nextAgg = (curAgg + delta).coerceAtLeast(0L)
                val nextGlobal = (curGlobal + delta).coerceAtLeast(0L)
                tx.set(
                    aggRef,
                    mapOf(FIELD_ADDED_TO_FAVORITE to nextAgg),
                    SetOptions.merge(),
                )
                tx.set(
                    globalRef,
                    mapOf(
                        FIELD_ADDED_TO_FAVORITE to nextGlobal,
                        FIELD_PRODUCT_ID to productId,
                        FIELD_STORE_ID to storeId,
                        FIELD_CATEGORY_ID to categoryId,
                    ),
                    SetOptions.merge(),
                )
                null
            }.await()
        }
    }

    suspend fun fetchProductStatsSnapshot(productId: String): Result<ProductStatsSnapshot> = runCatching {
        if (productId.isBlank()) return@runCatching ProductStatsSnapshot()
        val snap = db.collection(COLLECTION_PRODUCTS)
            .document(productId)
            .collection(SUBCOLLECTION_STATS)
            .document(STATS_DOC_ID)
            .get()
            .await()
        if (!snap.exists()) return@runCatching ProductStatsSnapshot()
        ProductStatsSnapshot(
            views = snap.getLong(FIELD_VIEWS) ?: 0L,
            addedToCart = snap.getLong(FIELD_ADDED_TO_CART) ?: 0L,
            addedToFavorite = snap.getLong(FIELD_ADDED_TO_FAVORITE) ?: 0L,
            purchased = snap.getLong(FIELD_PURCHASED) ?: 0L,
        )
    }

    private fun DocumentSnapshot.storeIdAndCategoryId(): Pair<String, String> {
        val storeId = getString(FIELD_STORE_ID).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val cat = get(FIELD_CATEGORY_MAP) as? Map<*, *>
        val categoryId = cat?.get(FIELD_CATEGORY_ID_KEY)?.toString()?.trim().orEmpty()
        return storeId to categoryId
    }

    companion object {
        const val COLLECTION_PRODUCTS = "products"
        const val SUBCOLLECTION_STATS = "stats"
        const val STATS_DOC_ID = "aggregate"
        const val COLLECTION_PRODUCT_STATS = "productStats"

        const val FIELD_VIEWS = "views"
        const val FIELD_ADDED_TO_CART = "addedToCart"
        const val FIELD_ADDED_TO_FAVORITE = "addedToFavorite"
        const val FIELD_PURCHASED = "purchased"
        const val FIELD_PRODUCT_ID = "productId"
        const val FIELD_STORE_ID = "storeId"
        const val FIELD_CATEGORY_ID = "categoryId"
        private const val FIELD_CATEGORY_MAP = "category"
        private const val FIELD_CATEGORY_ID_KEY = "categoryId"
    }
}
