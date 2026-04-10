package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AdminDashboardOverview
import com.example.myapplication.data.repository.AdminDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminDashboardUiState {
    data object Loading : AdminDashboardUiState
    data class Ready(val overview: AdminDashboardOverview) : AdminDashboardUiState
    data class Error(val message: String) : AdminDashboardUiState
}

class AdminDashboardViewModel(
    private val repository: AdminDashboardRepository = AdminDashboardRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
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
}
