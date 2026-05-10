package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Message
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.MessagingRepository
import com.example.myapplication.data.repository.StoreRepository
import com.example.myapplication.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

sealed class MessagingUiState {
    object Loading : MessagingUiState()
    data class Ready(
        val messages: List<Message>,
        val currentUserId: String,
        val otherParticipantName: String,
        val parentOrderId: String,
        /** False until a Firestore thread exists (first message creates it). */
        val threadExists: Boolean,
    ) : MessagingUiState()
    data class Error(val message: String) : MessagingUiState()
}

class MessagingViewModel(
    private val storeId: String,
    private val suborderId: String,
    private val messagingRepository: MessagingRepository = MessagingRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessagingUiState>(MessagingUiState.Loading)
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    private var chatId: String? = null
    private var messagesJob: Job? = null
    private var listeningChatId: String? = null

    private var resolvedCustomerId: String = ""
    private var resolvedParentOrderId: String = ""
    private var resolvedStoreName: String = ""
    private var resolvedCustomerName: String = ""
    private var otherParticipantDisplayName: String = ""

    private val firstSendMutex = Mutex()

    init {
        loadChat()
    }

    private fun emitReady(
        messages: List<Message>,
        user: FirebaseUser,
        threadExists: Boolean,
    ) {
        _uiState.value = MessagingUiState.Ready(
            messages = messages,
            currentUserId = user.uid,
            otherParticipantName = otherParticipantDisplayName,
            parentOrderId = resolvedParentOrderId,
            threadExists = threadExists,
        )
    }

    private fun attachMessagesListener(chatId: String, user: FirebaseUser) {
        if (listeningChatId == chatId && messagesJob?.isActive == true) return
        listeningChatId = chatId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            try {
                val revealCustomer = user.uid == resolvedCustomerId
                val ownerSid = storeRepository.getStoreIdForOwner(user.uid)
                val revealStore = ownerSid != null && ownerSid == storeId
                when {
                    revealCustomer -> messagingRepository.revealChatInInbox(chatId, revealCustomerInbox = true)
                    revealStore -> messagingRepository.revealChatInInbox(chatId, revealCustomerInbox = false)
                }
            } catch (_: Exception) {
                // Non-fatal if reveal fails (e.g. offline); chat still loads.
            }
            messagingRepository.listenToMessages(chatId)
                .catch { e ->
                    Log.e("MessagingVM", "Error collecting messages", e)
                    _uiState.value = MessagingUiState.Error("Failed to load messages: ${e.message}")
                }
                .collect { messages ->
                    emitReady(messages, user, threadExists = true)
                    messagingRepository.markAsRead(chatId, user.uid)
                }
        }
    }

    private fun loadChat() {
        viewModelScope.launch {
            try {
                val user = authRepository.auth.currentUser ?: throw Exception("User not logged in")
                val store = storeRepository.fetchStoreById(storeId) ?: throw Exception("Store not found")

                val suborderSnap = orderRepository.fetchSuborderById(suborderId) ?: throw Exception("Suborder not found")
                val parentOrderRef = suborderSnap.reference.parent.parent ?: throw Exception("Parent order not found")
                val orderSnap = parentOrderRef.get().await()
                val customerId = orderSnap.getString("userId") ?: throw Exception("Customer ID not found")

                val customerResult = authRepository.fetchUserProfile(customerId)
                val customerName = customerResult.getOrNull()?.name ?: "Customer"

                resolvedCustomerId = customerId
                resolvedParentOrderId = orderSnap.id
                resolvedStoreName = store.name
                resolvedCustomerName = customerName
                otherParticipantDisplayName = if (user.uid == customerId) store.name else customerName

                val existingId = messagingRepository.findChatThreadBySuborder(suborderId)
                chatId = existingId

                if (existingId != null) {
                    attachMessagesListener(existingId, user)
                } else {
                    emitReady(emptyList(), user, threadExists = false)
                }
            } catch (e: Exception) {
                _uiState.value = MessagingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                val user = authRepository.auth.currentUser ?: return@launch
                var createdInThisCall = false
                firstSendMutex.withLock {
                    if (chatId == null) {
                        val id = messagingRepository.createChatThreadWithFirstMessage(
                            suborderId = suborderId,
                            storeId = storeId,
                            customerId = resolvedCustomerId,
                            storeName = resolvedStoreName,
                            customerName = resolvedCustomerName,
                            parentOrderId = resolvedParentOrderId,
                            senderId = user.uid,
                            text = trimmed,
                        )
                        chatId = id
                        createdInThisCall = true
                        attachMessagesListener(id, user)
                    }
                }
                if (!createdInThisCall) {
                    val id = chatId ?: return@launch
                    messagingRepository.sendMessage(id, user.uid, trimmed)
                }
            } catch (e: Exception) {
                Log.e("MessagingVM", "sendMessage failed", e)
            }
        }
    }

    fun hideChatForCurrentUser(onComplete: () -> Unit) {
        viewModelScope.launch {
            val id = chatId
            val user = authRepository.auth.currentUser
            if (id == null || user == null) {
                onComplete()
                return@launch
            }
            try {
                val hideAsCustomer = user.uid == resolvedCustomerId
                val ownerStoreId = storeRepository.getStoreIdForOwner(user.uid)
                val hideAsStore = ownerStoreId != null && ownerStoreId == storeId
                when {
                    hideAsCustomer -> messagingRepository.hideChatForParticipant(id, hideAsCustomer = true)
                    hideAsStore -> messagingRepository.hideChatForParticipant(id, hideAsCustomer = false)
                    else -> Log.w("MessagingVM", "hideChat: user is neither customer nor store owner for this thread")
                }
            } catch (e: Exception) {
                Log.e("MessagingVM", "hideChat failed", e)
            } finally {
                onComplete()
            }
        }
    }

    override fun onCleared() {
        messagesJob?.cancel()
        super.onCleared()
    }
}

class MessagingViewModelFactory(
    private val storeId: String,
    private val suborderId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagingViewModel(storeId, suborderId) as T
    }
}
