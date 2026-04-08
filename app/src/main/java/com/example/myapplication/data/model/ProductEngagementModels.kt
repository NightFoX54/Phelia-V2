package com.example.myapplication.data.model

/** Sipariş kaleminde gömülü yorum (Firestore: orders/.../items içinde `review` map) */
data class OrderItemReviewEmb(
    val rating: Double,
    val comment: String,
    val createdAtMs: Long,
)

/** Firestore: products/{productId}/reviews/{reviewId} */
data class ProductReviewDoc(
    val reviewId: String,
    val userId: String,
    val productId: String,
    val orderId: String,
    val suborderId: String,
    val orderItemId: String,
    val rating: Double,
    val comment: String,
    val storeResponse: String?,
    val storeRespondedAtMs: Long,
    val createdAtMs: Long,
)

/** Firestore: products/{productId}/questions/{questionId} */
data class ProductQuestionDoc(
    val questionId: String,
    val userId: String,
    val question: String,
    val answer: String?,
    val createdAtMs: Long,
    val answeredAtMs: Long,
)

/** Yorum yazma için uygun tek satır (sipariş tamamlanmış, review yok) */
data class EligibleReviewSlot(
    val orderId: String,
    val suborderId: String,
    val itemId: String,
    val productId: String,
)
