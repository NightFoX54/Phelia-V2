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
    val auth: FirebaseAuth = FirebaseRemoteDataSource.auth,
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
        
        // Always allow store owners to sign in so they can see their notifications 
        // and application status in-app.
        return@runCatching StoreOwnerGateResult(allowed = true)
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
