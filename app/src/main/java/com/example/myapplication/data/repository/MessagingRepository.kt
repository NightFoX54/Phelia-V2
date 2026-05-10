package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.ChatThread
import com.example.myapplication.data.model.Message
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessagingRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {

    private fun sortMessagesChronological(messages: List<Message>): List<Message> =
        messages.sortedWith(
            compareBy<Message>(
                { it.timestamp.seconds },
                { it.timestamp.nanoseconds },
                { it.id },
            ),
        )

    /** Returns chat thread document id if one exists for this suborder, otherwise null. */
    suspend fun findChatThreadBySuborder(suborderId: String): String? {
        if (suborderId.isBlank()) return null
        val snap = db.collection("chats")
            .whereEqualTo("suborderId", suborderId)
            .limit(1)
            .get()
            .await()
        return snap.documents.firstOrNull()?.id
    }

    /**
     * Creates a new chat thread and the first message in one batch (no empty “placeholder” thread).
     */
    suspend fun createChatThreadWithFirstMessage(
        suborderId: String,
        storeId: String,
        customerId: String,
        storeName: String,
        customerName: String,
        parentOrderId: String,
        senderId: String,
        text: String,
    ): String {
        val chatRef = db.collection("chats").document()
        val messageRef = chatRef.collection("messages").document()
        val now = Timestamp.now()
        val thread = ChatThread(
            id = chatRef.id,
            suborderId = suborderId,
            parentOrderId = parentOrderId,
            storeId = storeId,
            customerId = customerId,
            storeName = storeName,
            customerName = customerName,
            lastMessage = text,
            lastMessageTimestamp = now,
            lastReadBy = mapOf(senderId to now),
        )
        val message = Message(
            id = messageRef.id,
            chatId = chatRef.id,
            senderId = senderId,
            text = text,
            timestamp = now,
        )
        db.runBatch { batch ->
            batch.set(chatRef, thread)
            batch.set(messageRef, message)
        }.await()

        val recipientId = if (senderId == thread.customerId) thread.storeId else thread.customerId
        val senderName = if (senderId == thread.customerId) thread.customerName else thread.storeName
        notificationRepository.sendToUser(
            userId = recipientId,
            type = NotificationTypes.NEW_MESSAGE,
            title = "New message from $senderName",
            body = text,
            orderId = thread.suborderId,
            storeId = thread.storeId
        )
        return chatRef.id
    }

    /**
     * Stream messages for a specific chat thread (oldest → newest).
     */
    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        var registration: ListenerRegistration? = null

        fun start(useOrderBy: Boolean) {
            val query = if (useOrderBy) {
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
            } else {
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
            }

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (useOrderBy) {
                        Log.w("MessagingRepository", "Messages index missing or error, falling back: ${error.message}")
                        registration?.remove()
                        start(false)
                    } else {
                        Log.e("MessagingRepository", "Error listening to messages: ${error.message}")
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                val messages = sortMessagesChronological(
                    snapshot?.documents?.mapNotNull { it.toObject(Message::class.java)?.copy(id = it.id) } ?: emptyList(),
                )
                trySend(messages)
            }
        }

        start(true)
        awaitClose { registration?.remove() }
    }

    /**
     * Send a message in an existing chat thread.
     */
    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val chatRef = db.collection("chats").document(chatId)
        val chatSnap = chatRef.get().await()
        val thread = chatSnap.toObject(ChatThread::class.java) ?: return

        val messageRef = chatRef.collection("messages").document()

        val message = Message(
            id = messageRef.id,
            chatId = chatId,
            senderId = senderId,
            text = text,
            timestamp = Timestamp.now()
        )

        db.runBatch { batch ->
            batch.set(messageRef, message)
            batch.update(
                chatRef,
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastReadBy.$senderId" to message.timestamp
                )
            )
        }.await()

        val recipientId = if (senderId == thread.customerId) thread.storeId else thread.customerId
        val senderName = if (senderId == thread.customerId) thread.customerName else thread.storeName

        notificationRepository.sendToUser(
            userId = recipientId,
            type = NotificationTypes.NEW_MESSAGE,
            title = "New message from $senderName",
            body = text,
            orderId = thread.suborderId,
            storeId = thread.storeId
        )
    }

    /** Hides the chat only for the customer or only for the store inbox (does not delete the thread). */
    suspend fun hideChatForParticipant(chatId: String, hideAsCustomer: Boolean) {
        val field = if (hideAsCustomer) "hiddenFromCustomer" else "hiddenFromStore"
        db.collection("chats").document(chatId).update(field, true).await()
    }

    /** Shows the thread again in that participant’s inbox (e.g. when opening chat from the order). */
    suspend fun revealChatInInbox(chatId: String, revealCustomerInbox: Boolean) {
        val field = if (revealCustomerInbox) "hiddenFromCustomer" else "hiddenFromStore"
        db.collection("chats").document(chatId).update(field, false).await()
    }

    /**
     * Stream all chat threads for a store owner.
     */
    fun listenToStoreChats(storeId: String): Flow<List<ChatThread>> = callbackFlow {
        var registration: ListenerRegistration? = null

        fun start(useOrderBy: Boolean) {
            val query = if (useOrderBy) {
                db.collection("chats")
                    .whereEqualTo("storeId", storeId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            } else {
                db.collection("chats")
                    .whereEqualTo("storeId", storeId)
            }

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (useOrderBy) {
                        Log.w("MessagingRepository", "Store chats index missing, falling back: ${error.message}")
                        registration?.remove()
                        start(false)
                    } else {
                        Log.e("MessagingRepository", "Error listening to store chats: ${error.message}")
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                var threads = snapshot?.documents?.mapNotNull { it.toObject(ChatThread::class.java)?.copy(id = it.id) } ?: emptyList()
                threads = threads.filter { !it.hiddenFromStore }
                if (!useOrderBy) {
                    threads = threads.sortedByDescending { it.lastMessageTimestamp }
                }
                trySend(threads)
            }
        }

        start(true)
        awaitClose { registration?.remove() }
    }

    /**
     * Stream all chat threads for a customer.
     */
    fun listenToCustomerChats(customerId: String): Flow<List<ChatThread>> = callbackFlow {
        var registration: ListenerRegistration? = null

        fun start(useOrderBy: Boolean) {
            val query = if (useOrderBy) {
                db.collection("chats")
                    .whereEqualTo("customerId", customerId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            } else {
                db.collection("chats")
                    .whereEqualTo("customerId", customerId)
            }

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (useOrderBy) {
                        Log.w("MessagingRepository", "Customer chats index missing, falling back: ${error.message}")
                        registration?.remove()
                        start(false)
                    } else {
                        Log.e("MessagingRepository", "Error listening to customer chats: ${error.message}")
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                var threads = snapshot?.documents?.mapNotNull { it.toObject(ChatThread::class.java)?.copy(id = it.id) } ?: emptyList()
                threads = threads.filter { !it.hiddenFromCustomer }
                if (!useOrderBy) {
                    threads = threads.sortedByDescending { it.lastMessageTimestamp }
                }
                trySend(threads)
            }
        }

        start(true)
        awaitClose { registration?.remove() }
    }

    /**
     * Mark a chat as read by a user.
     */
    suspend fun markAsRead(chatId: String, userId: String) {
        db.collection("chats").document(chatId)
            .update("lastReadBy.$userId", Timestamp.now())
            .await()
    }
}
