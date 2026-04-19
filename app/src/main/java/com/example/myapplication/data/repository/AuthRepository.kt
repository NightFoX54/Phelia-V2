package com.example.myapplication.data.repository

import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.data.model.toFirestoreMap
import com.example.myapplication.data.model.toUser
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseRemoteDataSource.auth,
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {
    data class StoreOwnerGateResult(
        val allowed: Boolean,
        val message: String? = null,
    )

    suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Unit
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e -> Result.failure(Exception(mapAuthError(e))) },
        )

    suspend fun checkEmailAvailability(email: String): Result<Unit> = runCatching {
        val emailTrim = email.trim()
        if (emailTrim.isBlank()) return@runCatching
        val existing = db.collection(COLLECTION_USERS)
            .whereEqualTo("email", emailTrim)
            .get()
            .await()
        if (!existing.isEmpty) {
            error("Choose another mail address")
        }
    }

    /** Creates Firebase Auth user + `users/{uid}`. Returns new [User.uid]. */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String = "customer",
    ): Result<String> =
        runCatching {
            val emailTrim = email.trim()
            val existing = db.collection(COLLECTION_USERS)
                .whereEqualTo("email", emailTrim)
                .get()
                .await()
            if (!existing.isEmpty) {
                error("Choose another mail address")
            }

            val result = auth.createUserWithEmailAndPassword(emailTrim, password).await()
            val uid = result.user?.uid ?: error("Could not create user")
            val user = User(
                uid = uid,
                name = name.trim(),
                email = emailTrim,
                role = role.trim().ifBlank { "customer" },
                createdAt = System.currentTimeMillis(),
            )
            db.collection(COLLECTION_USERS).document(uid).set(user.toFirestoreMap()).await()
            uid
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e -> Result.failure(Exception(mapAuthError(e))) },
        )

    suspend fun fetchUserProfile(uid: String): Result<User> {
        repeat(PROFILE_FETCH_RETRIES) { attempt ->
            val snap = runCatching {
                db.collection(COLLECTION_USERS).document(uid).get().await()
            }.getOrElse { return Result.failure(it) }
            if (snap.exists()) {
                return Result.success(snap.toUser())
            }
            delay(200L * (attempt + 1))
        }
        return Result.failure(
            Exception("users/$uid document not found. Create the profile in Firestore."),
        )
    }

    suspend fun evaluateStoreOwnerGate(uid: String): Result<StoreOwnerGateResult> = runCatching {
        if (uid.isBlank()) return@runCatching StoreOwnerGateResult(allowed = false, message = "Invalid session.")

        val hasStore = db.collection(COLLECTION_STORES)
            .whereEqualTo(FIELD_OWNER_ID, uid)
            .limit(1)
            .get()
            .await()
            .documents
            .isNotEmpty()
        if (hasStore) return@runCatching StoreOwnerGateResult(allowed = true)

        var appDocs = db.collection(COLLECTION_STORE_APPLICATIONS)
            .whereEqualTo(FIELD_APPLICANT_USER_ID, uid)
            .get()
            .await()
            .documents

        // Race condition fix: If the user just registered, the application document might take 
        // a few seconds to appear in Firestore. We retry with increasing delays.
        if (appDocs.isEmpty()) {
            for (i in 1..5) {
                delay(500L * i) 
                appDocs = db.collection(COLLECTION_STORE_APPLICATIONS)
                    .whereEqualTo(FIELD_APPLICANT_USER_ID, uid)
                    .get()
                    .await()
                    .documents
                if (appDocs.isNotEmpty()) break
            }
        }

        if (appDocs.isEmpty()) {
            return@runCatching StoreOwnerGateResult(
                allowed = false,
                message = "Your application is being processed. Please wait a moment and try signing in again.",
            )
        }
        val latest = appDocs.maxByOrNull { (it.getLong(FIELD_CREATED_AT) ?: 0L) }
        val status = latest?.getString(FIELD_STATUS)?.trim().orEmpty()
        when (status) {
            StoreApplication.STATUS_PENDING -> StoreOwnerGateResult(
                allowed = false,
                message = "Your store application has been received and is pending admin approval.",
            )
            StoreApplication.STATUS_REJECTED -> {
                val reason = latest?.getString(FIELD_REJECTION_REASON)?.trim().orEmpty()
                val msg = if (reason.isNotBlank()) {
                    "Your store application was rejected: $reason"
                } else {
                    "Your store application was rejected."
                }
                StoreOwnerGateResult(allowed = false, message = msg)
            }
            StoreApplication.STATUS_APPROVED -> StoreOwnerGateResult(
                allowed = false,
                message = "Your application has been approved, but your store setup is not completed yet. Please try again later.",
            )
            else -> StoreOwnerGateResult(
                allowed = false,
                message = "Could not read your store application status.",
            )
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun mapAuthError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        is FirebaseAuthUserCollisionException -> "Choose another mail address"
        is FirebaseAuthWeakPasswordException -> "Password is too weak (at least 6 characters)."
        else -> e.message ?: "Something went wrong."
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_STORE_APPLICATIONS = "storeApplications"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_APPLICANT_USER_ID = "applicantUserId"
        private const val FIELD_STATUS = "status"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_REJECTION_REASON = "rejectionReason"
        private const val PROFILE_FETCH_RETRIES = 8
    }
}
