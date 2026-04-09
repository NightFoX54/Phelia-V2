package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.model.COLLECTION_STORE_APPLICATIONS
import com.example.myapplication.data.model.FIELD_APPLICANT_USER_ID
import com.example.myapplication.data.model.FIELD_REJECTION_REASON
import com.example.myapplication.data.model.FIELD_REVIEWED_AT
import com.example.myapplication.data.model.FIELD_REVIEWED_BY_USER_ID
import com.example.myapplication.data.model.FIELD_STATUS
import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.data.model.toFirestoreMap
import com.example.myapplication.data.model.toStoreApplication
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.example.myapplication.util.StoreLogoImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StoreApplicationRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val storage: FirebaseStorage = FirebaseRemoteDataSource.storage,
) {

    /** Before account exists, no upload. After register, path: `store_application_uploads/{uid}/logo_*.jpg`. */
    suspend fun uploadApplicantStoreLogo(context: Context, applicantUid: String, sourceUri: Uri): Result<String> = runCatching {
        if (applicantUid.isBlank()) error("Missing user")
        val cropped = StoreLogoImageUtils.cropCenterSquareToJpegFile(context.applicationContext, sourceUri)
        val path = "store_application_uploads/$applicantUid/logo_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(cropped).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun ensureNoPendingApplication(applicantUserId: String): Result<Unit> = runCatching {
        if (applicantUserId.isBlank()) error("Missing user")
        val snap = db.collection(COLLECTION_STORE_APPLICATIONS)
            .whereEqualTo(FIELD_APPLICANT_USER_ID, applicantUserId)
            .whereEqualTo(FIELD_STATUS, StoreApplication.STATUS_PENDING)
            .limit(1)
            .get()
            .await()
        if (!snap.isEmpty) error("You already have a pending store application.")
    }

    suspend fun submitApplication(
        applicantUserId: String,
        applicantName: String,
        applicantEmail: String,
        storeName: String,
        storeDescription: String,
        storeLogoUrl: String,
    ): Result<String> = runCatching {
        if (storeName.isBlank()) error("Store name is required.")
        val ref = db.collection(COLLECTION_STORE_APPLICATIONS).document()
        val now = System.currentTimeMillis()
        val doc = StoreApplication(
            applicationId = ref.id,
            applicantUserId = applicantUserId,
            applicantName = applicantName,
            applicantEmail = applicantEmail,
            storeName = storeName.trim(),
            storeDescription = storeDescription.trim(),
            storeLogoUrl = storeLogoUrl.trim(),
            status = StoreApplication.STATUS_PENDING,
            createdAtMs = now,
        )
        ref.set(doc.toFirestoreMap()).await()
        ref.id
    }

    fun listenPendingApplications(
        onUpdate: (List<StoreApplication>) -> Unit,
        onError: ((String?) -> Unit)? = null,
    ): ListenerRegistration =
        db.collection(COLLECTION_STORE_APPLICATIONS)
            .whereEqualTo(FIELD_STATUS, StoreApplication.STATUS_PENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    onError?.invoke(err.message)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toStoreApplication() }
                    ?.sortedBy { it.createdAtMs } ?: emptyList()
                onError?.invoke(null)
                onUpdate(list)
            }

    suspend fun approveApplication(applicationId: String, adminUserId: String): Result<Unit> = runCatching {
        if (applicationId.isBlank() || adminUserId.isBlank()) error("Invalid request")
        val appRef = db.collection(COLLECTION_STORE_APPLICATIONS).document(applicationId)
        val preApp = appRef.get().await()
        if (!preApp.exists()) error("Application not found")
        val pre = preApp.toStoreApplication() ?: error("Invalid application")
        if (pre.status != StoreApplication.STATUS_PENDING) error("Application is not pending")
        val uid = pre.applicantUserId
        if (uid.isBlank()) error("Missing applicant")
        val existingStore = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, uid)
            .limit(1)
            .get()
            .await()
        if (!existingStore.isEmpty) error("User already has a store")

        db.runTransaction { tx ->
            val appSnap = tx.get(appRef)
            if (!appSnap.exists()) error("Application not found")
            val app = appSnap.toStoreApplication() ?: error("Invalid application")
            if (app.status != StoreApplication.STATUS_PENDING) error("Application is no longer pending")

            val userRef = db.collection(COLLECTION_USERS).document(uid)
            val userSnap = tx.get(userRef)
            if (!userSnap.exists()) error("User profile not found")

            val storeRef = db.collection(COLLECTION_STORES).document()
            val now = System.currentTimeMillis()
            tx.set(
                storeRef,
                mapOf(
                    FIELD_OWNER_ID to uid,
                    FIELD_NAME to app.storeName,
                    FIELD_DESCRIPTION to app.storeDescription,
                    FIELD_LOGO to app.storeLogoUrl,
                    FIELD_RATING to 0.0,
                    FIELD_REVIEW_COUNT to 0L,
                    FIELD_CREATED_AT to now,
                ),
            )
            tx.update(
                userRef,
                mapOf(FIELD_ROLE to ROLE_STORE_OWNER),
            )
            tx.update(
                appRef,
                mapOf(
                    FIELD_STATUS to StoreApplication.STATUS_APPROVED,
                    FIELD_REVIEWED_AT to now,
                    FIELD_REVIEWED_BY_USER_ID to adminUserId,
                    FIELD_REJECTION_REASON to "",
                ),
            )
            null
        }.await()
    }

    suspend fun rejectApplication(
        applicationId: String,
        adminUserId: String,
        reason: String,
    ): Result<Unit> = runCatching {
        if (applicationId.isBlank() || adminUserId.isBlank()) error("Invalid request")
        val appRef = db.collection(COLLECTION_STORE_APPLICATIONS).document(applicationId)
        val now = System.currentTimeMillis()
        db.runTransaction { tx ->
            val appSnap = tx.get(appRef)
            if (!appSnap.exists()) error("Application not found")
            val app = appSnap.toStoreApplication() ?: error("Invalid application")
            if (app.status != StoreApplication.STATUS_PENDING) error("Application is not pending")
            tx.update(
                appRef,
                mapOf(
                    FIELD_STATUS to StoreApplication.STATUS_REJECTED,
                    FIELD_REVIEWED_AT to now,
                    FIELD_REVIEWED_BY_USER_ID to adminUserId,
                    FIELD_REJECTION_REASON to reason.trim(),
                ),
            )
            null
        }.await()
    }

    companion object {
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_USERS = "users"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_NAME = "name"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_LOGO = "logo"
        private const val FIELD_RATING = "rating"
        private const val FIELD_REVIEW_COUNT = "reviewCount"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_ROLE = "role"
        private const val ROLE_STORE_OWNER = "store_owner"
    }
}
