package com.example.myapplication.data.model

/** Firestore: orders/{orderId} */
data class OrderDoc(
    val orderId: String,
    val userId: String,
    val totalPrice: Double,
    val totalTax: Double,
    val shippingFee: Double,
    val status: String,
    val paymentMethodId: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

/** Firestore: orders/{orderId}/suborders/{suborderId} */
data class SuborderDoc(
    val suborderId: String,
    val storeId: String,
    val status: String,
    val totalPrice: Double,
    val totalTax: Double,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

/** UI: tek sipariş detayı (Firestore + mağaza adı) */
data class OrderDetailBundle(
    val order: OrderDoc,
    val shippingAddressLines: List<String>,
    val suborders: List<SuborderDetailUi>,
)

data class SuborderDetailUi(
    val suborder: SuborderDoc,
    val storeName: String,
    val items: List<OrderItemDoc>,
)

/** Firestore: orders/{orderId}/suborders/{suborderId}/items/{itemId} */
data class OrderItemDoc(
    val itemId: String,
    val productId: String,
    val variantId: String,
    val name: String,
    val variant: Map<String, String>,
    val unitPrice: Double,
    val quantity: Int,
    val tax: Double,
    val createdAtMs: Long,
    /** Ürün sayfasındaki review ile senkron; yoksa kullanıcı henüz yorumlamadı */
    val review: OrderItemReviewEmb? = null,
)

/**
 * Firestore `status` alanı (İngilizce anahtarlar).
 * Eski kayıtlar: [legacyOrderStatusLabel] ile etiketlenir.
 */
object OrderStatus {
    const val ORDER_RECEIVED = "order_received"
    const val ORDER_CONFIRMED = "order_confirmed"
    const val PREPARING = "preparing"
    const val SHIPPED = "shipped"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

/** Kullanıcı arayüzünde gösterilecek kısa İngilizce metinler. */
fun orderStatusLabelEnglish(status: String): String = when (status) {
    OrderStatus.ORDER_RECEIVED, "pending" -> "Order received"
    OrderStatus.ORDER_CONFIRMED, "confirmed" -> "Order confirmed"
    OrderStatus.PREPARING, "processing" -> "Being prepared"
    OrderStatus.SHIPPED -> "Shipped"
    OrderStatus.COMPLETED, "delivered" -> "Completed"
    OrderStatus.CANCELLED -> "Cancelled"
    else -> status.split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) }
        .ifBlank { status }
}
