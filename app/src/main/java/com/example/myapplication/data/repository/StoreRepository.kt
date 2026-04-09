package com.example.myapplication.data.repository

import android.net.Uri
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
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

    suspend fun updateStoreForOwner(
        ownerId: String,
        name: String,
        description: String,
        logoUrl: String,
    ): Result<Unit> = runCatching {
        val snap = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, ownerId)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: error("No store found for this account.")
        doc.reference.update(
            mapOf(
                FIELD_NAME to name.trim(),
                FIELD_DESCRIPTION to description.trim(),
                FIELD_LOGO to logoUrl.trim(),
            ),
        ).await()
    }

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
        )

    companion object {
        private const val COLLECTION_STORES = "stores"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_NAME = "name"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_LOGO = "logo"
        private const val FIELD_RATING = "rating"
        private const val FIELD_REVIEW_COUNT = "reviewCount"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
