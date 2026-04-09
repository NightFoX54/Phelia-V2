package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.StoreOrderDetailBundle
import com.example.myapplication.data.model.allowedNextSuborderStatuses
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreOrderDetailViewModel(
    private val orderId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    /** Keeps the store orders list in sync after a status change (same Activity ViewModel). */
    private val invalidateStoreOrdersList: () -> Unit = {},
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoreOrderDetailUiState>(StoreOrderDetailUiState.Loading)
    val uiState: StateFlow<StoreOrderDetailUiState> = _uiState.asStateFlow()

    private val _updating = MutableStateFlow(false)
    val updating: StateFlow<Boolean> = _updating.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = StoreOrderDetailUiState.Error("Not signed in")
            return
        }
        viewModelScope.launch {
            _uiState.value = StoreOrderDetailUiState.Loading
            val storeId = storeRepository.getStoreIdForOwner(uid)
            if (storeId.isNullOrBlank()) {
                _uiState.value = StoreOrderDetailUiState.Error("No store for this account")
                return@launch
            }
            orderRepository.fetchStoreOrderDetailForOwner(orderId, storeId).fold(
                onSuccess = { bundle ->
                    val next = allowedNextSuborderStatuses(bundle.ourSuborder.status)
                    _uiState.value = StoreOrderDetailUiState.Ready(bundle, storeId, next)
                },
                onFailure = { _uiState.value = StoreOrderDetailUiState.Error(it.message ?: "Failed to load") },
            )
        }
    }

    fun updateSuborderStatus(newStatus: String) {
        val state = _uiState.value as? StoreOrderDetailUiState.Ready ?: return
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _updating.value = true
            _message.value = null
            val storeId = storeRepository.getStoreIdForOwner(uid) ?: run {
                _updating.value = false
                _message.value = "No store"
                return@launch
            }
            orderRepository.updateSuborderStatusForStore(
                orderId = orderId,
                suborderFirestoreId = state.bundle.ourSuborderFirestoreId,
                newStatus = newStatus,
                storeId = storeId,
            ).fold(
                onSuccess = {
                    _message.value = "Status updated"
                    orderRepository.fetchStoreOrderDetailForOwner(orderId, storeId).fold(
                        onSuccess = { bundle ->
                            val next = allowedNextSuborderStatuses(bundle.ourSuborder.status)
                            _uiState.value = StoreOrderDetailUiState.Ready(bundle, storeId, next)
                            invalidateStoreOrdersList()
                        },
                        onFailure = { e -> _message.value = e.message ?: "Reload failed" },
                    )
                },
                onFailure = { _message.value = it.message ?: "Update failed" },
            )
            _updating.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

sealed interface StoreOrderDetailUiState {
    data object Loading : StoreOrderDetailUiState
    data class Ready(
        val bundle: StoreOrderDetailBundle,
        val storeId: String,
        val allowedNextStatuses: List<String>,
    ) : StoreOrderDetailUiState
    data class Error(val message: String) : StoreOrderDetailUiState
}

class StoreOrderDetailViewModelFactory(
    private val orderId: String,
    private val invalidateStoreOrdersList: () -> Unit = {},
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StoreOrderDetailViewModel(
            orderId = orderId,
            invalidateStoreOrdersList = invalidateStoreOrdersList,
        ) as T
}
