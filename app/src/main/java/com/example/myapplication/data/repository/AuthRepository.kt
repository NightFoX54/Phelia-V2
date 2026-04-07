package com.example.myapplication.data.repository

import com.example.myapplication.data.model.User
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

    suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Unit
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e -> Result.failure(Exception(mapAuthError(e))) },
        )

    suspend fun register(name: String, email: String, password: String): Result<Unit> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = result.user?.uid ?: error("Kullanıcı oluşturulamadı")
            val user = User(
                uid = uid,
                name = name.trim(),
                email = email.trim(),
                role = "customer",
                createdAt = System.currentTimeMillis(),
            )
            db.collection(COLLECTION_USERS).document(uid).set(user.toFirestoreMap()).await()
            Unit
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
            Exception("users/$uid belgesi bulunamadı. Firestore’da profil oluşturun."),
        )
    }

    fun signOut() {
        auth.signOut()
    }

    private fun mapAuthError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "E-posta veya şifre hatalı."
        is FirebaseAuthUserCollisionException -> "Bu e-posta ile zaten kayıt var."
        is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf (en az 6 karakter)."
        else -> e.message ?: "Bir hata oluştu."
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val PROFILE_FETCH_RETRIES = 8
    }
}
