package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.repository.AdminCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminCatalogUiState {
    data object Loading : AdminCatalogUiState
    data class Ready(val categories: List<Category>, val brands: List<Brand>) : AdminCatalogUiState
    data class Error(val message: String) : AdminCatalogUiState
}

class AdminCatalogViewModel(
    private val repository: AdminCatalogRepository = AdminCatalogRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminCatalogUiState>(AdminCatalogUiState.Loading)
    val uiState: StateFlow<AdminCatalogUiState> = _uiState.asStateFlow()

    private val _banner = MutableStateFlow<String?>(null)
    val banner: StateFlow<String?> = _banner.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init {
        refresh()
    }

    fun dismissBanner() {
        _banner.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminCatalogUiState.Loading
            loadIntoState(onFailure = { _uiState.value = AdminCatalogUiState.Error(it) })
        }
    }

    private suspend fun loadIntoState(onFailure: (String) -> Unit) {
        repository.loadAll().fold(
            onSuccess = { (cats, brands) ->
                _uiState.value = AdminCatalogUiState.Ready(cats, brands)
            },
            onFailure = {
                onFailure(it.message ?: "Could not load categories and brands.")
            },
        )
    }

    private fun reloadAfterMutation() {
        viewModelScope.launch {
            loadIntoState(
                onFailure = { msg ->
                    _banner.value = msg
                    if (_uiState.value !is AdminCatalogUiState.Ready) {
                        _uiState.value = AdminCatalogUiState.Error(msg)
                    }
                },
            )
        }
    }

    fun saveCategory(
        isEdit: Boolean,
        categoryId: String?,
        name: String,
        attributesCsv: String,
        taxRateInput: String,
        onFinished: (Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            _saving.value = true
            try {
                val tax = taxRateInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
                val attrs = attributesCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                val result = if (isEdit && !categoryId.isNullOrBlank()) {
                    repository.updateCategory(categoryId, name, attrs, tax)
                } else {
                    repository.createCategory(name, attrs, tax).map { }
                }
                result.fold(
                    onSuccess = {
                        _banner.value = if (isEdit) "Category updated." else "Category created."
                        reloadAfterMutation()
                        onFinished(true)
                    },
                    onFailure = {
                        _banner.value = it.message ?: "Could not save category."
                        onFinished(false)
                    },
                )
            } finally {
                _saving.value = false
            }
        }
    }

    fun saveBrand(
        isEdit: Boolean,
        brandId: String?,
        name: String,
        onFinished: (Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            _saving.value = true
            try {
                val result = if (isEdit && !brandId.isNullOrBlank()) {
                    repository.updateBrand(brandId, name)
                } else {
                    repository.createBrand(name).map { }
                }
                result.fold(
                    onSuccess = {
                        _banner.value = if (isEdit) "Brand updated." else "Brand created."
                        reloadAfterMutation()
                        onFinished(true)
                    },
                    onFailure = {
                        _banner.value = it.message ?: "Could not save brand."
                        onFinished(false)
                    },
                )
            } finally {
                _saving.value = false
            }
        }
    }
}
