package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/paymentMethods/{paymentMethodId}
 * Full PAN and CVV are not stored; safe display fields like [maskedPan], [last4], and brand are kept.
 */
data class PaymentMethodDoc(
    val paymentMethodId: String,
    val label: String,
    val type: String,
    val brand: String,
    /** e.g. "4532 •••• •••• 4242" */
    val maskedPan: String,
    val last4: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
    val createdAtMs: Long,
)
