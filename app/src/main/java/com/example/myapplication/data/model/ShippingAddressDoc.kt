package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/shippingAddress/{addressId}
 */
data class ShippingAddressDoc(
    val addressId: String,
    val label: String,
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String,
    val district: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean,
    val createdAtMs: Long,
)
