package com.example.myapplication.data.model

import com.google.firebase.Timestamp

/** Customer → admin support ticket (Firestore: supportTickets/{id}). */
data class SupportTicket(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    /** What the customer typed (order id, ORD-…, etc.). */
    val orderReferenceRaw: String = "",
    /** Resolved orders/{id} for this customer, if matched. */
    val resolvedOrderId: String = "",
    val customerMessage: String = "",
    val status: String = STATUS_OPEN,
    val createdAt: Timestamp = Timestamp.now(),
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_CLOSED = "closed"
    }
}
