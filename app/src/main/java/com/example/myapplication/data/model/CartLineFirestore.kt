package com.example.myapplication.data.model

/**
 * Firestore: users/{uid}/cartItems/{documentId}
 * [documentId] = [cartDocId] (productId + "__" + variantId)
 */
data class CartLineFirestore(
    val documentId: String,
    val productId: String,
    val variantId: String,
    val quantity: Int,
)

fun cartDocId(productId: String, variantId: String): String = "${productId}__${variantId}"
