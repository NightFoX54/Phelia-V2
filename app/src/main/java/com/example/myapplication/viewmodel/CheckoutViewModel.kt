package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.model.ui.CartLineUi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val orderRepository: OrderRepository = OrderRepository(),
) : ViewModel() {

    private val _placing = MutableStateFlow(false)
    val placing: StateFlow<Boolean> = _placing.asStateFlow()

    fun placeOrder(
        lines: List<CartLineUi>,
        shippingAddress: ShippingAddressDoc,
        paymentMethodId: String,
        shippingFee: Double,
        onResult: (Result<String>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("No active session")))
            return
        }
        viewModelScope.launch {
            _placing.value = true
            val result = orderRepository.placeOrder(
                userId = uid,
                lines = lines,
                shippingAddress = shippingAddress,
                paymentMethodId = paymentMethodId,
                shippingFee = shippingFee,
            )
            _placing.value = false
            onResult(result)
        }
    }
}
