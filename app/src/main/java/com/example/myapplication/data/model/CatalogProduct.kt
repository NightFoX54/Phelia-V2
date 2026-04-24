package com.example.myapplication.data.model

data class CatalogProductSummary(
    val productId: String,
    val name: String,
    val categoryName: String?,
    val brandName: String?,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    /** Minimum final price among active variants. */
    val minPrice: Double,
    /** Base price (non-discounted) for the variant that produced [minPrice]. */
    val minBasePrice: Double,
    /** Discount percent (0..100) for the variant that produced [minPrice]. */
    val minDiscountPercent: Int,
)

data class ProductDetailBundle(
    val product: Product,
    val variantAttributeKeys: List<String>,
    val variants: List<ProductVariant>,
    /** Loaded from [Product.storeId] when present; null if missing or fetch failed. */
    val store: Store? = null,
)
