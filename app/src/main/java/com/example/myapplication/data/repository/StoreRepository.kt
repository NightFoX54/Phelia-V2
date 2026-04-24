package com.example.myapplication.data.repository

import android.net.Uri
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.example.myapplication.data.repository.NotificationRepository
import com.example.myapplication.data.repository.NotificationTypes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StoreRepository(
    private val auth: FirebaseAuth = FirebaseRemoteDataSource.auth,
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val storage: FirebaseStorage = FirebaseRemoteDataSource.storage,
) {

    fun listenStoreByOwner(
        ownerId: String,
        onUpdate: (Store?) -> Unit,
        onError: ((String?) -> Unit)? = null,
    ): ListenerRegistration =
        db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, ownerId)
            .limit(1)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(null)
                    onError?.invoke(err.message ?: "Could not load store")
                    return@addSnapshotListener
                }
                onError?.invoke(null)
                val doc = snap?.documents?.firstOrNull()
                onUpdate(doc?.toStore())
            }

    suspend fun fetchStoreById(storeId: String): Store? {
        if (storeId.isBlank()) return null
        val snap = db.collection(COLLECTION_STORES).document(storeId).get().await()
        if (!snap.exists()) return null
        return snap.toStore()
    }

    suspend fun getStoreIdForOwner(ownerId: String): String? {
        if (ownerId.isBlank()) return null
        val snap = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, ownerId)
            .limit(1)
            .get()
            .await()
        return snap.documents.firstOrNull()?.id
    }

    /** Uploads to `stores/{storeId}/brand/logo_{uuid}.jpg`; returns download URL. */
    suspend fun uploadStoreLogo(ownerId: String, localUri: Uri): Result<String> = runCatching {
        if (ownerId.isBlank()) error("Not signed in")
        val storeId = getStoreIdForOwner(ownerId) ?: error("No store found for this account.")
        auth.currentUser?.uid?.takeIf { it == ownerId } ?: error("Not signed in")
        val path = "stores/$storeId/brand/logo_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(localUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun submitStoreUpdateRequest(
        ownerId: String,
        name: String,
        description: String,
        logoUrl: String,
        email: String,
        phone: String,
        taxNumber: String,
        businessAddress: String,
    ): Result<Unit> = runCatching {
        val storeId = getStoreIdForOwner(ownerId) ?: error("No store found for this account.")
        
        val updateData = mapOf(
            "storeId" to storeId,
            "ownerId" to ownerId,
            "name" to name.trim(),
            "description" to description.trim(),
            "logo" to logoUrl.trim(),
            "email" to email.trim(),
            "phone" to phone.trim(),
            "taxNumber" to taxNumber.trim(),
            "businessAddress" to businessAddress.trim(),
            "status" to "pending",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection(COLLECTION_STORE_UPDATE_REQUESTS)
            .add(updateData)
            .await()

        // Notify admins
        val admins = db.collection("users").whereEqualTo("role", "admin").get().await()
        val notificationRepo = NotificationRepository(db)
        admins.documents.forEach { adminDoc ->
            notificationRepo.sendToUser(
                userId = adminDoc.id,
                type = NotificationTypes.STORE_UPDATE_REQUEST_SUBMITTED,
                title = "Store Update Request",
                body = "Store \"$name\" has submitted a change request for approval.",
                storeId = storeId
            )
        }
    }

    suspend fun updateStoreForOwner(
        ownerId: String,
        name: String,
        description: String,
        logoUrl: String,
    ): Result<Unit> = runCatching {
        val trimmedName = name.trim()
        val snap = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, ownerId)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: error("No store found for this account.")
        
        // Check if name changed and if new name is unique
        val currentName = doc.getString(FIELD_NAME).orEmpty()
        if (trimmedName != currentName) {
            // Check in stores
            val existingStore = db.collection(COLLECTION_STORES)
                .whereEqualTo(FIELD_NAME, trimmedName)
                .get()
                .await()
            if (!existingStore.isEmpty) error("Please select different store name")

            // Check in pending applications
            val existingApp = db.collection(COLLECTION_STORE_APPLICATIONS)
                .whereEqualTo("storeName", trimmedName)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            if (!existingApp.isEmpty) error("Please select different store name")
        }

        doc.reference.update(
            mapOf(
                FIELD_NAME to trimmedName,
                FIELD_DESCRIPTION to description.trim(),
                FIELD_LOGO to logoUrl.trim(),
            ),
        ).await()
    }

    suspend fun fetchAllStores(): List<Store> = runCatching {
        val snap = db.collection(COLLECTION_STORES).get().await()
        snap.documents.map { it.toStore() }
    }.getOrDefault(emptyList())

    private fun DocumentSnapshot.toStore(): Store =
        Store(
            storeId = id,
            ownerId = getString(FIELD_OWNER_ID).orEmpty(),
            name = getString(FIELD_NAME).orEmpty(),
            description = getString(FIELD_DESCRIPTION).orEmpty(),
            logo = getString(FIELD_LOGO).orEmpty(),
            rating = getDouble(FIELD_RATING) ?: 0.0,
            reviewCount = (getLong(FIELD_REVIEW_COUNT) ?: 0L).toInt(),
            createdAt = readMillis(FIELD_CREATED_AT),
            email = getString(FIELD_EMAIL).orEmpty(),
            phone = getString(FIELD_PHONE).orEmpty(),
            taxNumber = getString(FIELD_TAX_NUMBER).orEmpty(),
            businessAddress = getString(FIELD_BUSINESS_ADDRESS).orEmpty(),
        )

    companion object {
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_STORE_APPLICATIONS = "storeApplications"
        private const val COLLECTION_STORE_UPDATE_REQUESTS = "storeUpdateRequests"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_NAME = "name"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_LOGO = "logo"
        private const val FIELD_RATING = "rating"
        private const val FIELD_REVIEW_COUNT = "reviewCount"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_PHONE = "phone"
        private const val FIELD_TAX_NUMBER = "taxNumber"
        private const val FIELD_BUSINESS_ADDRESS = "businessAddress"
    }
}
