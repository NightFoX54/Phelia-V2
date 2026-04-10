package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AdminUserListItem
import com.example.myapplication.data.repository.AdminUserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUserManagementUiState {
    data object Loading : AdminUserManagementUiState
    data class Ready(val users: List<AdminUserListItem>) : AdminUserManagementUiState
    data class Error(val message: String) : AdminUserManagementUiState
}

class AdminUserManagementViewModel(
    private val repository: AdminUserManagementRepository = AdminUserManagementRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUserManagementUiState>(AdminUserManagementUiState.Loading)
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminUserManagementUiState.Loading
            repository.fetchUsers().fold(
                onSuccess = { _uiState.value = AdminUserManagementUiState.Ready(it) },
                onFailure = { _uiState.value = AdminUserManagementUiState.Error(it.message ?: "Could not load users.") },
            )
        }
    }
}
