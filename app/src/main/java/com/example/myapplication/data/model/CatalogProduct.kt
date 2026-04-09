package com.example.myapplication.data.model

data class CatalogProductSummary(
    val productId: String,
    val name: String,
    val categoryName: String?,
    val brandName: String?,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val minPrice: Double,
)

data class ProductDetailBundle(
    val product: Product,
    val variantAttributeKeys: List<String>,
    val variants: List<ProductVariant>,
    /** Loaded from [Product.storeId] when present; null if missing or fetch failed. */
    val store: Store? = null,
)
