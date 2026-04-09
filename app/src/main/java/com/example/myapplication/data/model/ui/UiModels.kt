package com.example.myapplication.data.model.ui

import androidx.compose.runtime.Immutable

enum class UserRole {
    CUSTOMER,
    STORE_OWNER,
    ADMIN,
}

@Immutable
data class User(
    val name: String,
    val email: String,
    val role: UserRole,
)

@Immutable
data class Category(
    val name: String,
    val isActive: Boolean = false,
)

@Immutable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val rating: Double,
    val reviewCount: Int = 0,
    val category: String? = null,
    val brandName: String? = null,
)

@Immutable
data class SelectedCartVariant(
    val variantId: String,
    val sku: String,
    val attributes: Map<String, String>,
    val unitPrice: Double,
)

@Immutable
data class CartItem(
    val product: Product,
    val quantity: Int,
    val variant: SelectedCartVariant? = null,
)

/** Cart screen row (Firestore cartItems + enriched display data). */
@Immutable
data class CartLineUi(
    val productId: String,
    val storeId: String,
    val variantId: String,
    val quantity: Int,
    val productName: String,
    val brandName: String?,
    val unitPrice: Double,
    val imageUrl: String,
    val attributes: Map<String, String>,
    val sku: String,
    val maxStock: Int,
)

@Immutable
data class Address(
    val id: String,
    val name: String,
    val addressLine: String,
    val cityLine: String,
    val phone: String,
)

@Immutable
data class PaymentMethod(
    val id: String,
    val type: String,
    val name: String,
    val emoji: String,
)

@Immutable
data class Review(
    val id: Long,
    val name: String,
    val initials: String,
    val rating: Int,
    val date: String,
    val comment: String,
    val helpful: Int,
)
