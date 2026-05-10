package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.SupportTicket
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class SupportTicketRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val orderRepository: OrderRepository = OrderRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
) {

    suspend fun submitTicket(
        customerId: String,
        customerName: String,
        customerEmail: String,
        orderReferenceRaw: String,
        customerMessage: String,
    ): Result<String> = runCatching {
        if (customerId.isBlank()) error("Not signed in")
        val msg = customerMessage.trim()
        if (msg.isBlank()) error("Please describe your issue")
        val refRaw = orderReferenceRaw.trim()
        if (refRaw.isBlank()) error("Paste your order number or ORD-… reference")

        val resolvedOrderId = orderRepository.resolveCustomerOrderId(customerId, refRaw).orEmpty()
        if (resolvedOrderId.isBlank()) {
            error(
                "We couldn't match that to one of your orders. Paste the full order ID or ORD-… label from Profile → My Orders.",
            )
        }

        val ref = db.collection(COLLECTION_SUPPORT_TICKETS).document()
        val payload = hashMapOf<String, Any>(
            FIELD_CUSTOMER_ID to customerId,
            FIELD_CUSTOMER_NAME to customerName.trim(),
            FIELD_CUSTOMER_EMAIL to customerEmail.trim(),
            FIELD_ORDER_REFERENCE_RAW to refRaw,
            FIELD_RESOLVED_ORDER_ID to resolvedOrderId,
            FIELD_CUSTOMER_MESSAGE to msg,
            FIELD_STATUS to SupportTicket.STATUS_OPEN,
            FIELD_CREATED_AT to FieldValue.serverTimestamp(),
        )
        ref.set(payload).await()

        val adminIds = db.collection(COLLECTION_USERS)
            .whereEqualTo(FIELD_ROLE, ROLE_ADMIN)
            .get()
            .await()
            .documents
            .map { it.id }
            .filter { it.isNotBlank() }

        val preview = msg.lines().firstOrNull()?.take(120) ?: msg.take(120)
        notificationRepository.sendToUsers(
            userIds = adminIds,
            type = NotificationTypes.SUPPORT_TICKET_SUBMITTED,
            title = "New support ticket",
            body = "${customerName.ifBlank { "Customer" }}: $preview",
            orderId = ref.id,
        ).onFailure { e -> Log.w(TAG, "Admin notify failed: ${e.message}") }

        ref.id
    }

    fun listenOpenTickets(
        onUpdate: (List<SupportTicket>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_SUPPORT_TICKETS)
            .whereEqualTo(FIELD_STATUS, SupportTicket.STATUS_OPEN)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Tickets listen error: ${err.message}")
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc -> doc.toSupportTicket() }.orEmpty()
                    .sortedByDescending { it.createdAt.seconds }
                onUpdate(list)
            }

    suspend fun fetchTicket(ticketId: String): SupportTicket? {
        if (ticketId.isBlank()) return null
        val doc = db.collection(COLLECTION_SUPPORT_TICKETS).document(ticketId).get().await()
        return doc.takeIf { it.exists() }?.toSupportTicket()
    }

    suspend fun updateTicketStatus(ticketId: String, status: String): Result<Unit> = runCatching {
        db.collection(COLLECTION_SUPPORT_TICKETS).document(ticketId)
            .update(FIELD_STATUS, status)
            .await()
    }

    private fun DocumentSnapshot.toSupportTicket(): SupportTicket? {
        val customerId = getString(FIELD_CUSTOMER_ID) ?: return null
        val created = getTimestamp(FIELD_CREATED_AT) ?: Timestamp.now()
        return SupportTicket(
            id = id,
            customerId = customerId,
            customerName = getString(FIELD_CUSTOMER_NAME).orEmpty(),
            customerEmail = getString(FIELD_CUSTOMER_EMAIL).orEmpty(),
            orderReferenceRaw = getString(FIELD_ORDER_REFERENCE_RAW).orEmpty(),
            resolvedOrderId = getString(FIELD_RESOLVED_ORDER_ID).orEmpty(),
            customerMessage = getString(FIELD_CUSTOMER_MESSAGE).orEmpty(),
            status = getString(FIELD_STATUS) ?: SupportTicket.STATUS_OPEN,
            createdAt = created,
        )
    }

    companion object {
        private const val TAG = "SupportTicketRepo"
        private const val COLLECTION_SUPPORT_TICKETS = "supportTickets"
        private const val COLLECTION_USERS = "users"
        private const val FIELD_ROLE = "role"
        private const val ROLE_ADMIN = "admin"
        private const val FIELD_CUSTOMER_ID = "customerId"
        private const val FIELD_CUSTOMER_NAME = "customerName"
        private const val FIELD_CUSTOMER_EMAIL = "customerEmail"
        private const val FIELD_ORDER_REFERENCE_RAW = "orderReferenceRaw"
        private const val FIELD_RESOLVED_ORDER_ID = "resolvedOrderId"
        private const val FIELD_CUSTOMER_MESSAGE = "customerMessage"
        private const val FIELD_STATUS = "status"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
