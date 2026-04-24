package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.ChatThread
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.repository.MessagingRepository
import com.example.myapplication.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job

class OrderHistoryViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: OrderRepository = OrderRepository(),
    private val messagingRepository: MessagingRepository = MessagingRepository(),
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderDoc>>(emptyList())
    val orders: StateFlow<List<OrderDoc>> = _orders.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatThread>>(emptyList())
    val chats: StateFlow<List<ChatThread>> = _chats.asStateFlow()

    private var ordersListener: ListenerRegistration? = null
    private var chatJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        ordersListener?.remove()
        ordersListener = null
        chatJob?.cancel()
        chatJob = null
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _orders.value = emptyList()
            _chats.value = emptyList()
        } else {
            ordersListener = repository.listenUserOrders(uid) { list ->
                _orders.value = list
            }
            chatJob = viewModelScope.launch {
                messagingRepository.listenToCustomerChats(uid)
                    .catch { e -> Log.e("OrderHistoryVM", "Error collecting customer chats", e) }
                    .collect {
                        _chats.value = it
                    }
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        ordersListener?.remove()
        chatJob?.cancel()
        super.onCleared()
    }
}
