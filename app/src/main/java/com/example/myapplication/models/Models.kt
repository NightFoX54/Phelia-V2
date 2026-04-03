package com.example.myapplication.models

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
    val category: String? = null,
)

@Immutable
data class ProductVariant(
    val colorName: String? = null,
    val storageName: String? = null,
)

@Immutable
data class CartItem(
    val product: Product,
    val quantity: Int,
    val variant: ProductVariant? = null,
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

