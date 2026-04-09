package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/favorites/{documentId}
 * Fields: favoriteId, productId, createdAt.
 * Document id matches [productId] for a single favorite per product (easy toggle).
 */
data class FavoriteEntry(
    val favoriteId: String,
    val productId: String,
    val createdAtMs: Long,
)
