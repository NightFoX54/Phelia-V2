package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.SupportTicket
import com.example.myapplication.data.repository.SupportTicketRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminSupportTicketsViewModel(
    private val repository: SupportTicketRepository = SupportTicketRepository(),
) : ViewModel() {

    private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val tickets: StateFlow<List<SupportTicket>> = _tickets.asStateFlow()

    private var listener: ListenerRegistration? = repository.listenOpenTickets { list ->
        _tickets.value = list
    }

    override fun onCleared() {
        listener?.remove()
        listener = null
        super.onCleared()
    }
}
