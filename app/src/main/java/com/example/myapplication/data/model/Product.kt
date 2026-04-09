package com.example.myapplication.data.model

data class Product(
    val productId: String = "",
    val storeId: String = "",
    val name: String = "",
    val description: String = "",
    val publicImages: List<String> = emptyList(),
    val brand: Map<String, String> = emptyMap(),
    val category: Map<String, String> = emptyMap(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Long = 0L,
    /** false = removed from sale (soft delete); past orders stay valid */
    val isActive: Boolean = true,
)
