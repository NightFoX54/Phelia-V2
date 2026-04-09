package com.example.myapplication.data.model

data class ProductVariant(
    val variantId: String = "",
    val sku: String = "",
    val attributes: Map<String, String> = emptyMap(),
    val price: Double = 0.0,
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    /** false = removed from sale; document is not deleted */
    val isActive: Boolean = true,
)
