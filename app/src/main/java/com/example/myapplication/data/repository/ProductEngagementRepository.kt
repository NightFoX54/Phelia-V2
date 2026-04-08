package com.example.myapplication.data.repository

import com.example.myapplication.data.model.OrderStatus
import com.example.myapplication.data.model.ProductQuestionDoc
import com.example.myapplication.data.model.ProductReviewDoc
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class ProductEngagementRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    fun listenProductReviews(
        productId: String,
        onUpdate: (List<ProductReviewDoc>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_REVIEWS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toProductReviewDoc() }
                    ?.sortedByDescending { it.createdAtMs } ?: emptyList()
                onUpdate(list)
            }

    fun listenProductQuestions(
        productId: String,
        onUpdate: (List<ProductQuestionDoc>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_QUESTIONS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toProductQuestionDoc() }
                    ?.sortedByDescending { it.createdAtMs } ?: emptyList()
                onUpdate(list)
            }

    suspend fun submitReview(
        userId: String,
        orderId: String,
        suborderId: String,
        itemId: String,
        productId: String,
        rating: Double,
        comment: String,
    ): Result<Unit> = runCatching {
        require(rating in 1.0..5.0) { "Rating must be 1–5" }
        val c = comment.trim()
        require(c.isNotEmpty()) { "Comment cannot be empty" }
        require(c.length <= 5000) { "Comment too long" }

        val orderRef = db.collection(COLLECTION_ORDERS).document(orderId)
        val itemRef = orderRef.collection(SUB_SUBORDERS).document(suborderId).collection(SUB_ITEMS).document(itemId)
        val reviewId = reviewDocumentId(orderId, suborderId, itemId)
        val reviewRef = db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_REVIEWS).document(reviewId)
        val productRef = db.collection(COLLECTION_PRODUCTS).document(productId)
        val now = FieldValue.serverTimestamp()

        db.runTransaction { tx ->
            val order = tx.get(orderRef)
            if (!order.exists()) error("Order not found")
            if (order.getString(FIELD_USER_ID) != userId) error("Not your order")
            val orderStatus = order.getString(FIELD_STATUS).orEmpty()
            if (orderStatus != OrderStatus.COMPLETED && orderStatus != "delivered") {
                error("You can only review products from completed orders")
            }

            val item = tx.get(itemRef)
            if (!item.exists()) error("Order item not found")
            if (item.getString(FIELD_PRODUCT_ID) != productId) error("Product mismatch")
            if (item.get(FIELD_REVIEW_EMB) != null) error("Already reviewed")

            val productSnap = tx.get(productRef)
            if (!productSnap.exists()) error("Product not found")
            val oldCount = (productSnap.getLong(FIELD_PRODUCT_REVIEW_COUNT) ?: 0L).toInt().coerceAtLeast(0)
            val oldRating = productSnap.getDouble(FIELD_PRODUCT_AGG_RATING) ?: 0.0
            val newCount = oldCount + 1
            val newRating = if (oldCount == 0) rating else (oldRating * oldCount + rating) / newCount

            tx.update(
                itemRef,
                mapOf(
                    FIELD_REVIEW_EMB to mapOf(
                        FIELD_REVIEW_RATING to rating,
                        FIELD_REVIEW_COMMENT to c,
                        FIELD_REVIEW_CREATED_AT to now,
                    ),
                ),
            )
            tx.set(
                reviewRef,
                mapOf(
                    FIELD_REVIEW_ID to reviewId,
                    FIELD_USER_ID to userId,
                    FIELD_PRODUCT_ID to productId,
                    FIELD_ORDER_ID to orderId,
                    FIELD_SUBORDER_ID to suborderId,
                    FIELD_ORDER_ITEM_ID to itemId,
                    FIELD_REVIEW_RATING to rating,
                    FIELD_REVIEW_COMMENT to c,
                    FIELD_STORE_RESPONSE to null,
                    FIELD_STORE_RESPONDED_AT to null,
                    FIELD_CREATED_AT to now,
                ),
            )
            tx.update(
                productRef,
                mapOf(
                    FIELD_PRODUCT_AGG_RATING to newRating,
                    FIELD_PRODUCT_REVIEW_COUNT to newCount,
                ),
            )
            null
        }.await()
    }

    suspend fun submitQuestion(
        userId: String,
        productId: String,
        question: String,
    ): Result<String> = runCatching {
        val q = question.trim()
        require(q.length >= 2) { "Question too short" }
        require(q.length <= 2000) { "Question too long" }
        val ref = db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_QUESTIONS).document()
        val id = ref.id
        val now = FieldValue.serverTimestamp()
        ref.set(
            mapOf(
                FIELD_QUESTION_ID to id,
                FIELD_USER_ID to userId,
                FIELD_QUESTION_TEXT to q,
                FIELD_ANSWER to null,
                FIELD_CREATED_AT to now,
                FIELD_ANSWERED_AT to null,
            ),
        ).await()
        id
    }

    suspend fun answerQuestionAsStore(
        ownerId: String,
        productId: String,
        questionId: String,
        answer: String,
    ): Result<Unit> = runCatching {
        verifyStoreOwnsProduct(ownerId, productId)
        val a = answer.trim()
        require(a.isNotEmpty()) { "Answer cannot be empty" }
        require(a.length <= 5000) { "Answer too long" }
        val qRef = db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_QUESTIONS).document(questionId)
        val now = FieldValue.serverTimestamp()
        qRef.update(
            mapOf(
                FIELD_ANSWER to a,
                FIELD_ANSWERED_AT to now,
            ),
        ).await()
    }

    suspend fun respondToReviewAsStore(
        ownerId: String,
        productId: String,
        reviewId: String,
        response: String,
    ): Result<Unit> = runCatching {
        verifyStoreOwnsProduct(ownerId, productId)
        val r = response.trim()
        require(r.isNotEmpty()) { "Response cannot be empty" }
        require(r.length <= 5000) { "Response too long" }
        val revRef = db.collection(COLLECTION_PRODUCTS).document(productId).collection(SUB_REVIEWS).document(reviewId)
        revRef.update(
            mapOf(
                FIELD_STORE_RESPONSE to r,
                FIELD_STORE_RESPONDED_AT to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    private suspend fun verifyStoreOwnsProduct(ownerId: String, productId: String) {
        val storeSnap = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_STORE_OWNER_ID, ownerId)
            .limit(1)
            .get()
            .await()
        val storeDoc = storeSnap.documents.firstOrNull() ?: error("No store for this account")
        val storeId = storeDoc.id
        val p = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        if (!p.exists()) error("Product not found")
        if (p.getString(FIELD_PRODUCT_STORE_ID) != storeId) error("This product is not in your store")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProductReviewDoc(): ProductReviewDoc? {
        val rid = getString("reviewId")?.takeIf { it.isNotBlank() } ?: id
        return ProductReviewDoc(
            reviewId = rid,
            userId = getString("userId").orEmpty(),
            productId = getString("productId").orEmpty(),
            orderId = getString("orderId").orEmpty(),
            suborderId = getString("suborderId").orEmpty(),
            orderItemId = getString("orderItemId").orEmpty(),
            rating = getDouble("rating") ?: 0.0,
            comment = getString("comment").orEmpty(),
            storeResponse = getString("storeResponse"),
            storeRespondedAtMs = readMillis("storeRespondedAt"),
            createdAtMs = readMillis("createdAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProductQuestionDoc(): ProductQuestionDoc? {
        val qid = getString("questionId")?.takeIf { it.isNotBlank() } ?: id
        val answeredRaw = get("answeredAt")
        val answeredMs = when (answeredRaw) {
            null -> 0L
            else -> readMillis("answeredAt")
        }
        return ProductQuestionDoc(
            questionId = qid,
            userId = getString("userId").orEmpty(),
            question = getString(FIELD_QUESTION_TEXT).orEmpty(),
            answer = getString(FIELD_ANSWER),
            createdAtMs = readMillis("createdAt"),
            answeredAtMs = answeredMs,
        )
    }

    companion object {
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_ORDERS = "orders"
        private const val FIELD_STORE_OWNER_ID = "ownerId"
        private const val FIELD_PRODUCT_STORE_ID = "storeId"
        private const val SUB_REVIEWS = "reviews"
        private const val SUB_QUESTIONS = "questions"
        private const val SUB_SUBORDERS = "suborders"
        private const val SUB_ITEMS = "items"

        private const val FIELD_USER_ID = "userId"
        private const val FIELD_STATUS = "status"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_REVIEW_EMB = "review"
        private const val FIELD_REVIEW_RATING = "rating"
        private const val FIELD_REVIEW_COMMENT = "comment"
        private const val FIELD_REVIEW_CREATED_AT = "createdAt"
        private const val FIELD_REVIEW_ID = "reviewId"
        private const val FIELD_ORDER_ID = "orderId"
        private const val FIELD_SUBORDER_ID = "suborderId"
        private const val FIELD_ORDER_ITEM_ID = "orderItemId"
        private const val FIELD_STORE_RESPONSE = "storeResponse"
        private const val FIELD_STORE_RESPONDED_AT = "storeRespondedAt"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_QUESTION_ID = "questionId"
        private const val FIELD_QUESTION_TEXT = "question"
        private const val FIELD_ANSWER = "answer"
        private const val FIELD_ANSWERED_AT = "answeredAt"

        private const val FIELD_PRODUCT_AGG_RATING = "rating"
        private const val FIELD_PRODUCT_REVIEW_COUNT = "reviewCount"

        fun reviewDocumentId(orderId: String, suborderId: String, itemId: String): String =
            listOf(orderId, suborderId, itemId)
                .joinToString("__")
                .replace("/", "_")
                .take(1400)
    }
}
