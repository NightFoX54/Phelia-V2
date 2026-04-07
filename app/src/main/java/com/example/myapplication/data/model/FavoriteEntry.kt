package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/favorites/{documentId}
 * Alanlar: favoriteId, productId, createdAt
 * [productId] ile ayni doc id kullanilir (tekil favori, toggle kolay).
 */
data class FavoriteEntry(
    val favoriteId: String,
    val productId: String,
    val createdAtMs: Long,
)
