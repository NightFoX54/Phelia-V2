package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.CartLineFirestore
import com.example.myapplication.data.model.ui.CartLineUi
import com.example.myapplication.data.repository.CartRepository
import com.example.myapplication.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val lines: List<CartLineUi> = emptyList(),
    val isEnriching: Boolean = false,
)

class CartViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val cartRepository: CartRepository = CartRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
) : ViewModel() {

    private val _rawLines = MutableStateFlow<List<CartLineFirestore>>(emptyList())
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _stockMessages = MutableStateFlow<List<String>>(emptyList())
    val stockMessages: StateFlow<List<String>> = _stockMessages.asStateFlow()

    private var cartListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        cartListener?.remove()
        cartListener = null
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _rawLines.value = emptyList()
            _uiState.value = CartUiState()
        } else {
            cartListener = cartRepository.listenCartLines(uid) { lines ->
                _rawLines.value = lines
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { uid ->
            cartListener = cartRepository.listenCartLines(uid) { lines ->
                _rawLines.value = lines
            }
        }
        viewModelScope.launch {
            _rawLines.collect { lines ->
                if (lines.isEmpty()) {
                    _uiState.value = CartUiState(lines = emptyList(), isEnriching = false)
                    return@collect
                }
                _uiState.value = _uiState.value.copy(isEnriching = true)
                productRepository.enrichCartLines(lines).fold(
                    onSuccess = { enriched ->
                        _uiState.value = CartUiState(lines = enriched, isEnriching = false)
                    },
                    onFailure = {
                        _uiState.value = CartUiState(lines = emptyList(), isEnriching = false)
                    },
                )
            }
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        cartListener?.remove()
        super.onCleared()
    }

    fun validateStockOnOpen() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            cartRepository.validateAndAdjustStock(uid).fold(
                onSuccess = { result ->
                    if (result.warnings.isNotEmpty()) {
                        _stockMessages.value = result.warnings
                    }
                },
                onFailure = { },
            )
        }
    }

    fun clearStockMessages() {
        _stockMessages.value = emptyList()
    }

    fun addToCart(productId: String, variantId: String, quantity: Int = 1) {
        val uid = auth.currentUser?.uid ?: return
        if (productId.isBlank() || variantId.isBlank() || quantity == 0) return
        viewModelScope.launch {
            cartRepository.addOrIncrement(uid, productId, variantId, quantity)
        }
    }

    fun changeQuantity(productId: String, variantId: String, delta: Int) {
        val uid = auth.currentUser?.uid ?: return
        if (delta == 0) return
        viewModelScope.launch {
            cartRepository.addOrIncrement(uid, productId, variantId, delta)
        }
    }

    fun remove(productId: String, variantId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            cartRepository.removeLine(uid, productId, variantId)
        }
    }
}
