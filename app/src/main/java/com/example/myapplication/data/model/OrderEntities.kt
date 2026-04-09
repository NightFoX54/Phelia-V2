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

/** UI: single order detail (Firestore + store name). */
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

/** Store owner order list row (this store's suborder only). */
data class StoreSuborderListRow(
    val orderId: String,
    val suborderFirestoreId: String,
    val suborder: SuborderDoc,
    val parentOrderStatus: String,
    val buyerDisplayName: String,
    val orderCreatedAtMs: Long,
    val itemCount: Int,
    val thumbnailUrl: String?,
)

/** Store order line + display image (product/variant). */
data class StoreOrderItemLine(
    val item: OrderItemDoc,
    val imageUrl: String?,
)

/** Store order detail (one store suborder + parent order summary). */
data class StoreOrderDetailBundle(
    val order: OrderDoc,
    val shippingAddressLines: List<String>,
    val buyerName: String,
    val buyerEmail: String,
    val ourSuborder: SuborderDoc,
    val ourSuborderFirestoreId: String,
    val items: List<StoreOrderItemLine>,
    val thumbnailUrl: String?,
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
    /** In sync with product-page review; null if the user has not reviewed yet. */
    val review: OrderItemReviewEmb? = null,
)

/**
 * Firestore `status` field (English keys).
 * Legacy records may use alternate strings; see [normalizeOrderStatus].
 */
object OrderStatus {
    const val ORDER_RECEIVED = "order_received"
    const val ORDER_CONFIRMED = "order_confirmed"
    const val PREPARING = "preparing"
    const val SHIPPED = "shipped"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
    /** Suborders are in mixed stages; parent cannot collapse to one status. */
    const val IN_PROGRESS = "in_progress"
}

/** Maps legacy or UI-sourced status strings to canonical keys. */
fun normalizeOrderStatus(raw: String): String {
    val k = raw.lowercase()
    return when (k) {
        "pending" -> OrderStatus.ORDER_RECEIVED
        "confirmed" -> OrderStatus.ORDER_CONFIRMED
        "processing" -> OrderStatus.PREPARING
        "delivered" -> OrderStatus.COMPLETED
        OrderStatus.ORDER_RECEIVED,
        OrderStatus.ORDER_CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.SHIPPED,
        OrderStatus.COMPLETED,
        OrderStatus.CANCELLED,
        OrderStatus.IN_PROGRESS,
        -> k
        else -> raw
    }
}

/**
 * Parent order status: same as all suborders if they match; otherwise [OrderStatus.IN_PROGRESS].
 * If all are cancelled, returns [OrderStatus.CANCELLED].
 */
fun aggregateParentOrderStatus(suborderStatuses: List<String>): String {
    if (suborderStatuses.isEmpty()) return OrderStatus.ORDER_RECEIVED
    val canon = suborderStatuses.map { normalizeOrderStatus(it) }
    if (canon.all { it == OrderStatus.CANCELLED }) return OrderStatus.CANCELLED
    val active = canon.filter { it != OrderStatus.CANCELLED }
    if (active.isEmpty()) return OrderStatus.CANCELLED
    return if (active.distinct().size == 1) active.first() else OrderStatus.IN_PROGRESS
}

/** Allowed next statuses for a store-owned suborder. */
fun allowedNextSuborderStatuses(current: String): List<String> {
    val c = normalizeOrderStatus(current)
    return when (c) {
        OrderStatus.ORDER_RECEIVED -> listOf(OrderStatus.ORDER_CONFIRMED, OrderStatus.CANCELLED)
        OrderStatus.ORDER_CONFIRMED -> listOf(OrderStatus.PREPARING, OrderStatus.CANCELLED)
        OrderStatus.PREPARING -> listOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
        OrderStatus.SHIPPED -> listOf(OrderStatus.COMPLETED)
        else -> emptyList()
    }
}

/** Short English labels for the UI. */
fun orderStatusLabelEnglish(status: String): String = when (status) {
    OrderStatus.ORDER_RECEIVED, "pending" -> "Order received"
    OrderStatus.ORDER_CONFIRMED, "confirmed" -> "Order confirmed"
    OrderStatus.PREPARING, "processing" -> "Being prepared"
    OrderStatus.SHIPPED -> "Shipped"
    OrderStatus.COMPLETED, "delivered" -> "Completed"
    OrderStatus.CANCELLED -> "Cancelled"
    OrderStatus.IN_PROGRESS -> "In progress"
    else -> status.split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) }
        .ifBlank { status }
}
