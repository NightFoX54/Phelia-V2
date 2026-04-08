package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/paymentMethods/{paymentMethodId}
 * Tam PAN ve CVV saklanmaz; [maskedPan], [last4] ve marka gibi güvenli gösterim alanları tutulur.
 */
data class PaymentMethodDoc(
    val paymentMethodId: String,
    val label: String,
    val type: String,
    val brand: String,
    /** Örn. "4532 •••• •••• 4242" */
    val maskedPan: String,
    val last4: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
    val createdAtMs: Long,
)
