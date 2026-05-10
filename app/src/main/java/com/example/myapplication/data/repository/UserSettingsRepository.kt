package com.example.myapplication.data.repository

import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.model.User
import com.example.myapplication.ui.theme.ThemePreference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class UserSettings(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val orderUpdates: Boolean = true,
)

data class UserNotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val orderId: String,
    val productId: String,
    val storeApplicationId: String,
    val storeId: String,
    val questionId: String,
    val createdAtMs: Long,
    val isRead: Boolean,
)

class UserSettingsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchUserProfile(userId: String): Result<User> = runCatching {
        if (userId.isBlank()) error("Not signed in")
        val doc = db.collection(COLLECTION_USERS).document(userId).get().await()
        if (!doc.exists()) error("User not found")
        doc.toObject(User::class.java)!!
    }

    suspend fun checkEmailAvailability(email: String, currentUserId: String): Result<Boolean> = runCatching {
        val query = db.collection(COLLECTION_USERS)
            .whereEqualTo("email", email)
            .get()
            .await()
        
        // If email found, it must belong to the current user
        query.documents.all { it.id == currentUserId }
    }

    suspend fun updateUserProfile(userId: String, name: String, email: String, phone: String, bio: String): Result<Unit> = runCatching {
        db.collection(COLLECTION_USERS).document(userId).update(
            mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "bio" to bio
            )
        ).await()
    }

    suspend fun fetchSettings(userId: String): Result<UserSettings> = runCatching {
        if (userId.isBlank()) error("Not signed in")
        val doc = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_SETTINGS).document(DOC_PREFERENCES)
            .get()
            .await()
        if (!doc.exists()) return@runCatching UserSettings()
        val storedTheme = ThemePreference.fromStorage(doc.getString(FIELD_THEME_PREFERENCE))
        val themePreference =
            storedTheme ?: ThemePreference.migrateFromLegacyDarkMode(doc.getBoolean(FIELD_DARK_MODE))
        UserSettings(
            themePreference = themePreference,
            orderUpdates = doc.getBoolean(FIELD_ORDER_UPDATES) ?: true,
        )
    }

    suspend fun saveSettings(userId: String, settings: UserSettings): Result<Unit> = runCatching {
        if (userId.isBlank()) error("Not signed in")
        db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_SETTINGS).document(DOC_PREFERENCES)
            .set(
                mapOf(
                    FIELD_THEME_PREFERENCE to settings.themePreference.storageKey,
                    FIELD_ORDER_UPDATES to settings.orderUpdates,
                    FIELD_UPDATED_AT to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    fun listenNotifications(
        userId: String,
        onUpdate: (List<UserNotificationItem>) -> Unit,
    ): com.google.firebase.firestore.ListenerRegistration =
        db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    UserNotificationItem(
                        id = d.id,
                        type = d.getString(FIELD_TYPE).orEmpty(),
                        title = d.getString(FIELD_TITLE).orEmpty().ifBlank { "Notification" },
                        body = d.getString(FIELD_BODY).orEmpty(),
                        orderId = d.getString(FIELD_ORDER_ID).orEmpty(),
                        productId = d.getString(FIELD_PRODUCT_ID).orEmpty(),
                        storeApplicationId = d.getString(FIELD_STORE_APPLICATION_ID).orEmpty(),
                        storeId = d.getString(FIELD_STORE_ID).orEmpty(),
                        questionId = d.getString(FIELD_QUESTION_ID).orEmpty(),
                        createdAtMs = d.readMillis(FIELD_CREATED_AT),
                        isRead = d.getBoolean(FIELD_IS_READ) == true,
                    )
                }?.sortedByDescending { it.createdAtMs } ?: emptyList()
                onUpdate(list)
            }

    suspend fun fetchNotifications(userId: String): Result<List<UserNotificationItem>> = runCatching {
        if (userId.isBlank()) error("Not signed in")
        val snap = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS)
            .get()
            .await()
        snap.documents.map { d ->
            UserNotificationItem(
                id = d.id,
                type = d.getString(FIELD_TYPE).orEmpty(),
                title = d.getString(FIELD_TITLE).orEmpty().ifBlank { "Notification" },
                body = d.getString(FIELD_BODY).orEmpty(),
                orderId = d.getString(FIELD_ORDER_ID).orEmpty(),
                productId = d.getString(FIELD_PRODUCT_ID).orEmpty(),
                storeApplicationId = d.getString(FIELD_STORE_APPLICATION_ID).orEmpty(),
                storeId = d.getString(FIELD_STORE_ID).orEmpty(),
                questionId = d.getString(FIELD_QUESTION_ID).orEmpty(),
                createdAtMs = d.readMillis(FIELD_CREATED_AT),
                isRead = d.getBoolean(FIELD_IS_READ) == true,
            )
        }.sortedByDescending { it.createdAtMs }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> = runCatching {
        if (userId.isBlank() || notificationId.isBlank()) error("Invalid request")
        db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS).document(notificationId)
            .update(FIELD_IS_READ, true)
            .await()
    }

    /**
     * Marks unread notifications matching [type] and optional payload fields (exact match).
     * Fetches unread notifications of [type] then filters in memory so Firestore composite indexes are not required.
     */
    suspend fun markUnreadNotificationsMatching(
        userId: String,
        type: String,
        orderId: String? = null,
        productId: String? = null,
        storeApplicationId: String? = null,
        storeId: String? = null,
        questionId: String? = null,
    ): Result<Unit> = runCatching {
        if (userId.isBlank() || type.isBlank()) error("Invalid request")
        val snap = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS)
            .whereEqualTo(FIELD_IS_READ, false)
            .get()
            .await()

        val docs = snap.documents.filter { d ->
            if (d.getString(FIELD_TYPE) != type) return@filter false
            val matchOrder = orderId.isNullOrBlank() || d.getString(FIELD_ORDER_ID) == orderId
            val matchProduct = productId.isNullOrBlank() || d.getString(FIELD_PRODUCT_ID) == productId
            val matchApp = storeApplicationId.isNullOrBlank() || d.getString(FIELD_STORE_APPLICATION_ID) == storeApplicationId
            val matchStore = storeId.isNullOrBlank() || d.getString(FIELD_STORE_ID) == storeId
            val matchQuestion = questionId.isNullOrBlank() || d.getString(FIELD_QUESTION_ID) == questionId
            matchOrder && matchProduct && matchApp && matchStore && matchQuestion
        }
        if (docs.isEmpty()) return@runCatching

        val batch = db.batch()
        docs.forEach { doc ->
            batch.update(doc.reference, FIELD_IS_READ, true)
        }
        batch.commit().await()
    }

    suspend fun markAllNotificationsAsRead(userId: String): Result<Unit> = runCatching {
        if (userId.isBlank()) error("Invalid request")
        val unread = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS)
            .whereEqualTo(FIELD_IS_READ, false)
            .get()
            .await()

        if (unread.isEmpty) return@runCatching

        val batch = db.batch()
        unread.documents.forEach { doc ->
            batch.update(doc.reference, FIELD_IS_READ, true)
        }
        batch.commit().await()
    }

    suspend fun deleteNotification(userId: String, notificationId: String): Result<Unit> = runCatching {
        if (userId.isBlank() || notificationId.isBlank()) error("Invalid request")
        db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_NOTIFICATIONS).document(notificationId)
            .delete()
            .await()
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_SETTINGS = "settings"
        private const val DOC_PREFERENCES = "preferences"
        private const val SUBCOLLECTION_NOTIFICATIONS = "notifications"

        private const val FIELD_DARK_MODE = "darkMode"
        private const val FIELD_THEME_PREFERENCE = "themePreference"
        private const val FIELD_ORDER_UPDATES = "orderUpdates"
        private const val FIELD_UPDATED_AT = "updatedAt"

        private const val FIELD_TITLE = "title"
        private const val FIELD_TYPE = "type"
        private const val FIELD_BODY = "body"
        private const val FIELD_ORDER_ID = "orderId"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_STORE_APPLICATION_ID = "storeApplicationId"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_QUESTION_ID = "questionId"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_IS_READ = "isRead"
    }
}
