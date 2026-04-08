package com.example.myapplication.data.model

/** Mağaza paneli ürün listesi satırı (Firestore `products` + `variants` özet). */
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
