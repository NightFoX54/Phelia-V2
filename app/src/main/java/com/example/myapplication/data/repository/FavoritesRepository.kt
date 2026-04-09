package com.example.myapplication.data.repository

import com.example.myapplication.data.model.FavoriteEntry
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FavoritesRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    fun listenFavorites(
        userId: String,
        onUpdate: (List<FavoriteEntry>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_USERS).document(userId).collection(SUBCOLLECTION_FAVORITES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    FavoriteEntry(
                        favoriteId = doc.getString(FIELD_FAVORITE_ID).orEmpty().ifBlank { doc.id },
                        productId = doc.getString(FIELD_PRODUCT_ID).orEmpty().ifBlank { doc.id },
                        createdAtMs = doc.readMillis("createdAt"),
                    )
                }?.sortedByDescending { it.createdAtMs } ?: emptyList()
                onUpdate(list)
            }

    /** @return `true` if the product is favorited after the toggle, `false` if removed. */
    suspend fun toggleFavorite(userId: String, productId: String): Result<Boolean> = runCatching {
        val ref = db.collection(COLLECTION_USERS).document(userId)
            .collection(SUBCOLLECTION_FAVORITES).document(productId)
        val snap = ref.get().await()
        if (snap.exists()) {
            ref.delete().await()
            false
        } else {
            ref.set(
                mapOf(
                    FIELD_FAVORITE_ID to UUID.randomUUID().toString(),
                    FIELD_PRODUCT_ID to productId,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            true
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_FAVORITES = "favorites"
        private const val FIELD_FAVORITE_ID = "favoriteId"
        private const val FIELD_PRODUCT_ID = "productId"
    }
}
