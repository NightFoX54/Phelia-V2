package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.SupportTicketRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelpSupportViewModel @JvmOverloads constructor(
    application: Application,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val supportTicketRepository: SupportTicketRepository = SupportTicketRepository(),
) : AndroidViewModel(application) {

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerEmail = MutableStateFlow("")
    val customerEmail: StateFlow<String> = _customerEmail.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitUi>(SubmitUi.Idle)
    val submitState: StateFlow<SubmitUi> = _submitState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            authRepository.fetchUserProfile(uid).onSuccess { u ->
                _customerName.value = u.name
                _customerEmail.value = u.email
            }
        }
    }

    fun submitTicket(orderReference: String, message: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _submitState.value = SubmitUi.Error("Please sign in again.")
            return
        }
        viewModelScope.launch {
            _submitState.value = SubmitUi.Sending
            supportTicketRepository.submitTicket(
                customerId = uid,
                customerName = _customerName.value,
                customerEmail = _customerEmail.value,
                orderReferenceRaw = orderReference,
                customerMessage = message,
            ).fold(
                onSuccess = {
                    _submitState.value = SubmitUi.Success
                },
                onFailure = { e ->
                    _submitState.value = SubmitUi.Error(e.message ?: "Could not send ticket")
                },
            )
        }
    }

    fun consumeSubmitMessage() {
        _submitState.value = SubmitUi.Idle
    }

    sealed interface SubmitUi {
        data object Idle : SubmitUi
        data object Sending : SubmitUi
        data object Success : SubmitUi
        data class Error(val message: String) : SubmitUi
    }
}
