package com.example.myapplication.data.model

/** Store panel product list row (Firestore `products` + `variants` summary). */
data class StoreOwnerProductRow(
    val productId: String,
    val name: String,
    val categoryName: String,
    val imageUrl: String,
    val minPrice: Double,
    val totalStock: Int,
    val variantCount: Int,
    val reviewCount: Int = 0,
    val isActive: Boolean = true,
)
