package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.data.model.StoreWeeklySalesSummary
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.data.repository.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreProductsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val storeRepository: StoreRepository = StoreRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
) : ViewModel() {

    private val _rows = MutableStateFlow<List<StoreOwnerProductRow>>(emptyList())
    val rows: StateFlow<List<StoreOwnerProductRow>> = _rows.asStateFlow()

    private val _loadState = MutableStateFlow<StoreProductsLoadState>(StoreProductsLoadState.Idle)
    val loadState: StateFlow<StoreProductsLoadState> = _loadState.asStateFlow()

    private val _weeklySales = MutableStateFlow<StoreWeeklySalesLoadState>(StoreWeeklySalesLoadState.Idle)
    val weeklySales: StateFlow<StoreWeeklySalesLoadState> = _weeklySales.asStateFlow()

    private val _salesRangeDays = MutableStateFlow(7)
    val salesRangeDays: StateFlow<Int> = _salesRangeDays.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var productsListener: ListenerRegistration? = null
    private var refreshJob: Job? = null
    private var activeStoreId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        productsListener?.remove()
        productsListener = null
        refreshJob?.cancel()
        activeStoreId = null
        _rows.value = emptyList()
        _weeklySales.value = StoreWeeklySalesLoadState.Idle
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _loadState.value = StoreProductsLoadState.Idle
            return@AuthStateListener
        }
        viewModelScope.launch {
            _loadState.value = StoreProductsLoadState.Loading
            val storeId = storeRepository.getStoreIdForOwner(uid)
            if (storeId.isNullOrBlank()) {
                _loadState.value = StoreProductsLoadState.NoStore
                return@launch
            }
            activeStoreId = storeId
            _loadState.value = StoreProductsLoadState.Ready
            attachProductsListener(storeId)
            refreshWeeklySales()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun attachProductsListener(storeId: String) {
        productsListener?.remove()
        productsListener = db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(FIELD_STORE_ID, storeId)
            .addSnapshotListener { _, err ->
                if (err != null) {
                    _loadState.value = StoreProductsLoadState.Error(err.message ?: "Could not load products")
                    return@addSnapshotListener
                }
                refreshJob?.cancel()
                refreshJob = viewModelScope.launch {
                    _loadState.value = StoreProductsLoadState.Ready
                    productRepository.fetchStoreOwnerProductRows(storeId).fold(
                        onSuccess = { _rows.value = it },
                        onFailure = { e ->
                            _loadState.value = StoreProductsLoadState.Error(e.message ?: "Could not load products")
                        },
                    )
                }
            }
    }

    fun refresh() {
        val sid = activeStoreId ?: return
        viewModelScope.launch {
            productRepository.fetchStoreOwnerProductRows(sid).fold(
                onSuccess = { _rows.value = it },
                onFailure = { },
            )
            refreshWeeklySales()
        }
    }

    fun refreshWeeklySales(days: Int = _salesRangeDays.value) {
        val sid = activeStoreId ?: return
        _salesRangeDays.value = days
        viewModelScope.launch {
            _weeklySales.value = StoreWeeklySalesLoadState.Loading
            orderRepository.fetchStoreSalesForRange(sid, days).fold(
                onSuccess = { _weeklySales.value = StoreWeeklySalesLoadState.Ready(it) },
                onFailure = { e ->
                    _weeklySales.value = StoreWeeklySalesLoadState.Error(
                        e.message ?: "Could not load sales",
                    )
                },
            )
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun deactivateProduct(productId: String) {
        if (productId.isBlank()) return
        viewModelScope.launch {
            productRepository.deactivateProductForCurrentOwner(productId).fold(
                onSuccess = { _userMessage.value = "Product removed from sale (not deleted)." },
                onFailure = { _userMessage.value = it.message ?: "Action failed" },
            )
        }
    }

    fun activateProduct(productId: String) {
        if (productId.isBlank()) return
        viewModelScope.launch {
            productRepository.activateProductForCurrentOwner(productId).fold(
                onSuccess = { _userMessage.value = "Product is for sale again." },
                onFailure = { _userMessage.value = it.message ?: "Action failed" },
            )
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        productsListener?.remove()
        refreshJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val COLLECTION_PRODUCTS = "products"
        private const val FIELD_STORE_ID = "storeId"
    }
}

sealed interface StoreProductsLoadState {
    data object Idle : StoreProductsLoadState
    data object Loading : StoreProductsLoadState
    data object Ready : StoreProductsLoadState
    data object NoStore : StoreProductsLoadState
    data class Error(val message: String) : StoreProductsLoadState
}

sealed interface StoreWeeklySalesLoadState {
    data object Idle : StoreWeeklySalesLoadState
    data object Loading : StoreWeeklySalesLoadState
    data class Ready(val summary: StoreWeeklySalesSummary) : StoreWeeklySalesLoadState
    data class Error(val message: String) : StoreWeeklySalesLoadState
}
