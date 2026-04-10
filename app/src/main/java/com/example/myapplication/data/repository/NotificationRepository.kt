package com.example.myapplication.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

object NotificationTypes {
    const val NEW_ORDER_FOR_STORE = "new_order_for_store"
    const val ORDER_STATUS_UPDATED = "order_status_updated"
    const val PRICE_DROP = "price_drop"
    const val STORE_APPLICATION_SUBMITTED = "store_application_submitted"
}

class NotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun sendToUser(
        userId: String,
        type: String,
        title: String,
        body: String,
        orderId: String? = null,
        productId: String? = null,
        storeApplicationId: String? = null,
    ): Result<Unit> = runCatching {
        if (userId.isBlank()) return@runCatching
        val ref = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS).document()
        ref.set(
            mapOf(
                FIELD_NOTIFICATION_ID to ref.id.ifBlank { UUID.randomUUID().toString() },
                FIELD_TYPE to type,
                FIELD_TITLE to title,
                FIELD_BODY to body,
                FIELD_ORDER_ID to orderId.orEmpty(),
                FIELD_PRODUCT_ID to productId.orEmpty(),
                FIELD_STORE_APPLICATION_ID to storeApplicationId.orEmpty(),
                FIELD_IS_READ to false,
                FIELD_CREATED_AT to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun sendToUsers(
        userIds: Collection<String>,
        type: String,
        title: String,
        body: String,
        orderId: String? = null,
        productId: String? = null,
        storeApplicationId: String? = null,
    ): Result<Unit> = runCatching {
        userIds.filter { it.isNotBlank() }.distinct().forEach { uid ->
            sendToUser(
                userId = uid,
                type = type,
                title = title,
                body = body,
                orderId = orderId,
                productId = productId,
                storeApplicationId = storeApplicationId,
            ).getOrThrow()
        }
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_NOTIFICATIONS = "notifications"
        private const val FIELD_NOTIFICATION_ID = "notificationId"
        private const val FIELD_TYPE = "type"
        private const val FIELD_TITLE = "title"
        private const val FIELD_BODY = "body"
        private const val FIELD_ORDER_ID = "orderId"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_STORE_APPLICATION_ID = "storeApplicationId"
        private const val FIELD_IS_READ = "isRead"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
