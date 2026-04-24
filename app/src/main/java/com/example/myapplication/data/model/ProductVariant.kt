package com.example.myapplication.data.model

data class ProductVariant(
    val variantId: String = "",
    val sku: String = "",
    val attributes: Map<String, String> = emptyMap(),
    val price: Double = 0.0,
    /** 0..100 percentage off the base [price]. */
    val discountPercent: Int = 0,
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    /** false = removed from sale; document is not deleted */
    val isActive: Boolean = true,
)

fun ProductVariant.finalPrice(): Double {
    val pct = discountPercent.coerceIn(0, 100)
    return price * (1.0 - (pct / 100.0))
}
