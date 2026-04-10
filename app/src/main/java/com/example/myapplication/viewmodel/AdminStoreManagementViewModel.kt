package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AdminStoreDetail
import com.example.myapplication.data.repository.AdminStoreListItem
import com.example.myapplication.data.repository.AdminStoreManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminStoreListUiState {
    data object Loading : AdminStoreListUiState
    data class Ready(val stores: List<AdminStoreListItem>) : AdminStoreListUiState
    data class Error(val message: String) : AdminStoreListUiState
}

class AdminStoreManagementViewModel(
    private val repository: AdminStoreManagementRepository = AdminStoreManagementRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminStoreListUiState>(AdminStoreListUiState.Loading)
    val uiState: StateFlow<AdminStoreListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminStoreListUiState.Loading
            repository.fetchStores().fold(
                onSuccess = { _uiState.value = AdminStoreListUiState.Ready(it) },
                onFailure = { _uiState.value = AdminStoreListUiState.Error(it.message ?: "Could not load stores.") },
            )
        }
    }
}

sealed interface AdminStoreDetailUiState {
    data object Loading : AdminStoreDetailUiState
    data class Ready(val detail: AdminStoreDetail) : AdminStoreDetailUiState
    data class Error(val message: String) : AdminStoreDetailUiState
}

class AdminStoreDetailViewModel(
    private val storeId: String,
    private val repository: AdminStoreManagementRepository = AdminStoreManagementRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminStoreDetailUiState>(AdminStoreDetailUiState.Loading)
    val uiState: StateFlow<AdminStoreDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminStoreDetailUiState.Loading
            repository.fetchStoreDetail(storeId).fold(
                onSuccess = { _uiState.value = AdminStoreDetailUiState.Ready(it) },
                onFailure = { _uiState.value = AdminStoreDetailUiState.Error(it.message ?: "Could not load store details.") },
            )
        }
    }
}

class AdminStoreDetailViewModelFactory(
    private val storeId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminStoreDetailViewModel(storeId) as T
}
