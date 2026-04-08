package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.PaymentMethodDoc
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.repository.PaymentMethodInput
import com.example.myapplication.data.repository.ShippingAddressInput
import com.example.myapplication.data.repository.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerAccountViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: UserAccountRepository = UserAccountRepository(),
) : ViewModel() {

    private val _shippingAddresses = MutableStateFlow<List<ShippingAddressDoc>>(emptyList())
    val shippingAddresses: StateFlow<List<ShippingAddressDoc>> = _shippingAddresses.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethodDoc>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethodDoc>> = _paymentMethods.asStateFlow()

    private var shippingListener: ListenerRegistration? = null
    private var paymentListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        attachListeners(user?.uid)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        attachListeners(auth.currentUser?.uid)
    }

    private fun attachListeners(uid: String?) {
        shippingListener?.remove()
        paymentListener?.remove()
        shippingListener = null
        paymentListener = null
        if (uid.isNullOrBlank()) {
            _shippingAddresses.value = emptyList()
            _paymentMethods.value = emptyList()
            return
        }
        shippingListener = repository.listenShippingAddresses(uid) { _shippingAddresses.value = it }
        paymentListener = repository.listenPaymentMethods(uid) { _paymentMethods.value = it }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        shippingListener?.remove()
        paymentListener?.remove()
        super.onCleared()
    }

    fun saveShippingAddress(
        existingAddressId: String?,
        input: ShippingAddressInput,
        onResult: (Result<String>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.saveShippingAddress(uid, existingAddressId, input))
        }
    }

    fun deleteShippingAddress(addressId: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.deleteShippingAddress(uid, addressId))
        }
    }

    fun setDefaultShippingAddress(addressId: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.setDefaultShippingAddress(uid, addressId))
        }
    }

    fun savePaymentMethod(
        existingPaymentMethodId: String?,
        input: PaymentMethodInput,
        onResult: (Result<String>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.savePaymentMethod(uid, existingPaymentMethodId, input))
        }
    }

    fun deletePaymentMethod(paymentMethodId: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.deletePaymentMethod(uid, paymentMethodId))
        }
    }

    fun setDefaultPaymentMethod(paymentMethodId: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.setDefaultPaymentMethod(uid, paymentMethodId))
        }
    }
}
