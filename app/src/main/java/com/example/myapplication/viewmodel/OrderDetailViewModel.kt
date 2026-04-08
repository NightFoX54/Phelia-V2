package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.ProductEngagementRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Ready(val data: OrderDetailBundle) : OrderDetailUiState
    data class Failed(val message: String) : OrderDetailUiState
}

class OrderDetailViewModel(
    private val orderId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: OrderRepository = OrderRepository(),
    private val engagementRepository: ProductEngagementRepository = ProductEngagementRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = OrderDetailUiState.Loading
        load()
    }

    fun submitProductReview(
        suborderId: String,
        itemId: String,
        productId: String,
        rating: Double,
        comment: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            val r = engagementRepository.submitReview(
                userId = uid,
                orderId = orderId,
                suborderId = suborderId,
                itemId = itemId,
                productId = productId,
                rating = rating,
                comment = comment,
            )
            r.onSuccess { retry() }
            onResult(r)
        }
    }

    private fun load() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _uiState.value = OrderDetailUiState.Failed("Not signed in")
                return@launch
            }
            repository.fetchOrderDetail(orderId, uid).fold(
                onSuccess = { _uiState.value = OrderDetailUiState.Ready(it) },
                onFailure = { e ->
                    _uiState.value = OrderDetailUiState.Failed(e.message ?: "Could not load order")
                },
            )
        }
    }
}
