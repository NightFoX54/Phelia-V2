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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class MessagingUiState {
    object Loading : MessagingUiState()
    data class Ready(
        val messages: List<Message>,
        val currentUserId: String,
        val otherParticipantName: String
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

    init {
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            try {
                val user = authRepository.auth.currentUser ?: throw Exception("User not logged in")
                val store = storeRepository.fetchStoreById(storeId) ?: throw Exception("Store not found")
                
                // Fetch suborder to find the customer ID
                val suborderSnap = orderRepository.fetchSuborderById(suborderId) ?: throw Exception("Suborder not found")
                val parentOrderRef = suborderSnap.reference.parent.parent ?: throw Exception("Parent order not found")
                val orderSnap = parentOrderRef.get().await()
                val customerId = orderSnap.getString("userId") ?: throw Exception("Customer ID not found")
                
                // Fetch customer name
                val customerResult = authRepository.fetchUserProfile(customerId)
                val customerName = customerResult.getOrNull()?.name ?: "Customer"

                chatId = messagingRepository.getOrCreateChatThread(
                    suborderId = suborderId,
                    storeId = storeId,
                    customerId = customerId,
                    storeName = store.name,
                    customerName = customerName
                )

                val otherParticipantName = if (user.uid == customerId) store.name else customerName

                messagingRepository.listenToMessages(chatId!!)
                    .catch { e -> 
                        Log.e("MessagingVM", "Error collecting messages", e)
                        _uiState.value = MessagingUiState.Error("Failed to load messages: ${e.message}")
                    }
                    .collect { messages ->
                        _uiState.value = MessagingUiState.Ready(messages, user.uid, otherParticipantName)
                        messagingRepository.markAsRead(chatId!!, user.uid)
                    }
            } catch (e: Exception) {
                _uiState.value = MessagingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendMessage(text: String) {
        val id = chatId ?: return
        viewModelScope.launch {
            try {
                val user = authRepository.auth.currentUser ?: return@launch
                messagingRepository.sendMessage(id, user.uid, text)
            } catch (e: Exception) {
                // Handle error
            }
        }
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
