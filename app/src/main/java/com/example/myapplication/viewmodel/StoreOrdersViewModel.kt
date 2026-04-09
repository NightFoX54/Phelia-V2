package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.StoreSuborderListRow
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreOrdersViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
) : ViewModel() {

    private val _rows = MutableStateFlow<List<StoreSuborderListRow>>(emptyList())
    val rows: StateFlow<List<StoreSuborderListRow>> = _rows.asStateFlow()

    private val _loadState = MutableStateFlow<StoreOrdersLoadState>(StoreOrdersLoadState.Idle)
    val loadState: StateFlow<StoreOrdersLoadState> = _loadState.asStateFlow()

    private var subListener: ListenerRegistration? = null
    private var enrichJob: Job? = null
    private var activeStoreId: String? = null

    private val authListener = FirebaseAuth.AuthStateListener { user ->
        subListener?.remove()
        subListener = null
        enrichJob?.cancel()
        enrichJob = null
        activeStoreId = null
        _rows.value = emptyList()
        if (user == null) {
            _loadState.value = StoreOrdersLoadState.Idle
            return@AuthStateListener
        }
        viewModelScope.launch {
            _loadState.value = StoreOrdersLoadState.Loading
            val sid = storeRepository.getStoreIdForOwner(user.uid.orEmpty())
            if (sid.isNullOrBlank()) {
                _loadState.value = StoreOrdersLoadState.NoStore
                return@launch
            }
            _loadState.value = StoreOrdersLoadState.Ready
            attachListener(sid)
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        auth.currentUser?.let { u ->
            viewModelScope.launch {
                _loadState.value = StoreOrdersLoadState.Loading
                val sid = storeRepository.getStoreIdForOwner(u.uid)
                if (sid.isNullOrBlank()) {
                    _loadState.value = StoreOrdersLoadState.NoStore
                    return@launch
                }
                _loadState.value = StoreOrdersLoadState.Ready
                attachListener(sid)
            }
        }
    }

    private fun attachListener(storeId: String) {
        subListener?.remove()
        activeStoreId = storeId
        subListener = orderRepository.listenStoreSuborders(storeId) { docs ->
            enrichJob?.cancel()
            enrichJob = viewModelScope.launch {
                try {
                    val enriched = orderRepository.enrichStoreSuborderDocuments(docs)
                    _rows.value = enriched.sortedByDescending { it.orderCreatedAtMs }
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    _loadState.value = StoreOrdersLoadState.Error(e.message ?: "Could not load orders")
                }
            }
        }
    }

    /**
     * Call when the store orders screen becomes visible again (e.g. back from detail).
     * Re-fetches from the server so list/parent order status stay in sync even if the listener lagged.
     */
    fun refreshOrdersIfPossible() {
        val sid = activeStoreId ?: return
        viewModelScope.launch {
            try {
                val docs = orderRepository.fetchStoreSubordersSnapshot(sid)
                val enriched = orderRepository.enrichStoreSuborderDocuments(docs)
                _rows.value = enriched.sortedByDescending { it.orderCreatedAtMs }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _loadState.value = StoreOrdersLoadState.Error(e.message ?: "Could not load orders")
            }
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        enrichJob?.cancel()
        subListener?.remove()
        super.onCleared()
    }
}

sealed interface StoreOrdersLoadState {
    data object Idle : StoreOrdersLoadState
    data object Loading : StoreOrdersLoadState
    data object Ready : StoreOrdersLoadState
    data object NoStore : StoreOrdersLoadState
    data class Error(val message: String) : StoreOrdersLoadState
}
