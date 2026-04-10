package com.example.myapplication.data.repository

import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.data.model.readMillis
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AdminUserListItem(
    val uid: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAtMs: Long,
    val ownedStoreName: String?,
    val pendingStoreApplication: Boolean,
    val pendingStoreName: String?,
)

class AdminUserManagementRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchUsers(): Result<List<AdminUserListItem>> = runCatching {
        val usersSnap = db.collection(COLLECTION_USERS).get().await()
        val storesSnap = db.collection(COLLECTION_STORES).get().await()
        val appsSnap = db.collection(COLLECTION_STORE_APPLICATIONS).get().await()

        val storeNameByOwner = storesSnap.documents.associate { doc ->
            doc.getString(FIELD_OWNER_ID).orEmpty() to doc.getString(FIELD_NAME).orEmpty()
        }.filterKeys { it.isNotBlank() }

        val latestAppByUser = appsSnap.documents
            .groupBy { it.getString(FIELD_APPLICANT_USER_ID).orEmpty() }
            .mapValues { (_, docs) ->
                docs.maxByOrNull { it.readMillis(FIELD_CREATED_AT) }
            }

        usersSnap.documents.map { doc ->
            val uid = doc.id
            val role = doc.getString(FIELD_ROLE).orEmpty().ifBlank { ROLE_CUSTOMER }
            val latestApp = latestAppByUser[uid]
            val appStatus = latestApp?.getString(FIELD_STATUS).orEmpty()
            AdminUserListItem(
                uid = uid,
                name = doc.getString(FIELD_NAME).orEmpty().ifBlank { "Unnamed user" },
                email = doc.getString(FIELD_EMAIL).orEmpty(),
                role = role,
                createdAtMs = doc.readMillis(FIELD_CREATED_AT),
                ownedStoreName = storeNameByOwner[uid]?.takeIf { it.isNotBlank() },
                pendingStoreApplication = appStatus == StoreApplication.STATUS_PENDING,
                pendingStoreName = latestApp?.getString(FIELD_STORE_NAME)?.takeIf { it.isNotBlank() },
            )
        }.sortedByDescending { it.createdAtMs }
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_STORE_APPLICATIONS = "storeApplications"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_APPLICANT_USER_ID = "applicantUserId"
        private const val FIELD_NAME = "name"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_ROLE = "role"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_STATUS = "status"
        private const val FIELD_STORE_NAME = "storeName"
        private const val ROLE_CUSTOMER = "customer"
    }
}
