package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AdminDashboardOverview
import com.example.myapplication.data.repository.AdminDashboardRepository
import com.example.myapplication.data.repository.AdminStoreManagementRepository
import com.example.myapplication.data.repository.StoreApplicationRepository
import com.example.myapplication.data.repository.SupportTicketRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Live counts of admin action items (matches list screens, not notification tray). */
data class AdminPendingCounts(
    val openSupportTickets: Int = 0,
    val pendingStoreApplications: Int = 0,
    val pendingStoreUpdateRequests: Int = 0,
)

sealed interface AdminDashboardUiState {
    data object Loading : AdminDashboardUiState
    data class Ready(val overview: AdminDashboardOverview) : AdminDashboardUiState
    data class Error(val message: String) : AdminDashboardUiState
}

class AdminDashboardViewModel(
    private val repository: AdminDashboardRepository = AdminDashboardRepository(),
    private val supportTicketRepository: SupportTicketRepository = SupportTicketRepository(),
    private val storeApplicationRepository: StoreApplicationRepository = StoreApplicationRepository(),
    private val storeManagementRepository: AdminStoreManagementRepository = AdminStoreManagementRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val _pendingCounts = MutableStateFlow(AdminPendingCounts())
    val pendingCounts: StateFlow<AdminPendingCounts> = _pendingCounts.asStateFlow()

    private var ticketsListener: ListenerRegistration? = null
    private var applicationsListener: ListenerRegistration? = null
    private var updateRequestsListener: ListenerRegistration? = null

    init {
        refresh()
        ticketsListener = supportTicketRepository.listenOpenTickets { list ->
            _pendingCounts.value = _pendingCounts.value.copy(openSupportTickets = list.size)
        }
        applicationsListener = storeApplicationRepository.listenPendingApplications(
            onUpdate = { list ->
                _pendingCounts.value = _pendingCounts.value.copy(pendingStoreApplications = list.size)
            },
        )
        updateRequestsListener = storeManagementRepository.listenPendingUpdateRequests(
            onUpdate = { list ->
                _pendingCounts.value = _pendingCounts.value.copy(pendingStoreUpdateRequests = list.size)
            },
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.Loading
            repository.fetchOverview().fold(
                onSuccess = { _uiState.value = AdminDashboardUiState.Ready(it) },
                onFailure = { _uiState.value = AdminDashboardUiState.Error(it.message ?: "Could not load dashboard data.") },
            )
        }
    }

    override fun onCleared() {
        ticketsListener?.remove()
        applicationsListener?.remove()
        updateRequestsListener?.remove()
        super.onCleared()
    }
}
