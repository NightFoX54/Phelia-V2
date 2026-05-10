package com.example.myapplication.data.model

import com.google.firebase.Timestamp

/**
 * Represents a single message in a chat thread.
 * Firestore: messages/{messageId}
 */
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * Represents a chat thread between a customer and a store regarding a specific suborder.
 * Firestore: chats/{chatId}
 */
data class ChatThread(
    val id: String = "",
    val suborderId: String = "",
    /** Parent Firestore order document id (orders/{id}); used for navigation and copy. */
    val parentOrderId: String = "",
    val storeId: String = "",
    val customerId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val storeName: String = "",
    val customerName: String = "",
    val lastReadBy: Map<String, Timestamp> = emptyMap(),
    /** Hidden only for this customer in chat lists (thread remains for the store). */
    val hiddenFromCustomer: Boolean = false,
    /** Hidden only for this store in chat lists (thread remains for the customer). */
    val hiddenFromStore: Boolean = false,
)
