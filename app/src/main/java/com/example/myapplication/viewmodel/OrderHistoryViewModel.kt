package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderHistoryViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: OrderRepository = OrderRepository(),
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderDoc>>(emptyList())
    val orders: StateFlow<List<OrderDoc>> = _orders.asStateFlow()

    private var ordersListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        ordersListener?.remove()
        ordersListener = null
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _orders.value = emptyList()
        } else {
            ordersListener = repository.listenUserOrders(uid) { list ->
                _orders.value = list
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        ordersListener?.remove()
        super.onCleared()
    }
}
