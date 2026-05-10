package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.model.SupportTicket
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.SupportTicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminSupportTicketDetailUiState {
    data object Loading : AdminSupportTicketDetailUiState
    data class Ready(val ticket: SupportTicket, val order: OrderDetailBundle?) : AdminSupportTicketDetailUiState
    data class Failed(val message: String) : AdminSupportTicketDetailUiState
}

class AdminSupportTicketDetailViewModel(
    private val ticketId: String,
    private val ticketRepository: SupportTicketRepository = SupportTicketRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<AdminSupportTicketDetailUiState>(AdminSupportTicketDetailUiState.Loading)
    val state: StateFlow<AdminSupportTicketDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = AdminSupportTicketDetailUiState.Loading
            val ticket = ticketRepository.fetchTicket(ticketId)
            if (ticket == null) {
                _state.value = AdminSupportTicketDetailUiState.Failed("Ticket not found")
                return@launch
            }
            val bundle = ticket.resolvedOrderId.takeIf { it.isNotBlank() }?.let { oid ->
                orderRepository.fetchOrderDetailForAdmin(oid).getOrNull()
            }
            _state.value = AdminSupportTicketDetailUiState.Ready(ticket, bundle)
        }
    }

    suspend fun markClosed() {
        ticketRepository.updateTicketStatus(ticketId, SupportTicket.STATUS_CLOSED)
    }
}

class AdminSupportTicketDetailViewModelFactory(
    private val ticketId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AdminSupportTicketDetailViewModel(ticketId) as T
}
