package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.ui.Category
import com.example.myapplication.data.model.ui.Product as UiProduct
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublicStoreUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val store: Store? = null,
    val searchQuery: String = "",
    val categories: List<Category> = emptyList(),
    val products: List<UiProduct> = emptyList(),
    val allStoreProducts: List<UiProduct> = emptyList(),
)

class PublicStoreViewModel(
    private val storeId: String,
    private val storeRepository: StoreRepository = StoreRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicStoreUiState())
    val uiState: StateFlow<PublicStoreUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val store = runCatching { storeRepository.fetchStoreById(storeId) }.getOrNull()
            if (store == null) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Store not found.", store = null, products = emptyList())
                }
                return@launch
            }
            val productsResult = productRepository.fetchCatalogSummariesForStore(storeId)
            if (productsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = productsResult.exceptionOrNull()?.message ?: "Could not load products.",
                        store = store,
                        products = emptyList(),
                    )
                }
                return@launch
            }
            val uiProducts = productsResult.getOrNull().orEmpty().map { s ->
                UiProduct(
                    id = s.productId,
                    name = s.name,
                    price = s.minPrice,
                    basePrice = s.minBasePrice,
                    discountPercent = s.minDiscountPercent,
                    imageUrl = s.imageUrl,
                    rating = s.rating,
                    reviewCount = s.reviewCount,
                    category = s.categoryName,
                    brandName = s.brandName,
                )
            }.sortedBy { it.name.lowercase() }

            val distinctCategories = uiProducts.mapNotNull { it.category }.distinct().sorted()
            val chips = listOf(Category(name = "All", isActive = true)) +
                    distinctCategories.map { Category(name = it, isActive = false) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    store = store,
                    products = uiProducts,
                    allStoreProducts = uiProducts,
                    categories = chips
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun setActiveCategory(name: String) {
        _uiState.update { state ->
            state.copy(
                categories = state.categories.map { it.copy(isActive = it.name == name) }
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        _uiState.update { state ->
            val activeCategory = state.categories.find { it.isActive }?.name ?: "All"
            var filtered = if (activeCategory == "All") {
                state.allStoreProducts
            } else {
                state.allStoreProducts.filter { it.category == activeCategory }
            }

            if (state.searchQuery.isNotBlank()) {
                filtered = filtered.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true) ||
                    (it.brandName?.contains(state.searchQuery, ignoreCase = true) == true)
                }
            }
            state.copy(products = filtered)
        }
    }
}

class PublicStoreViewModelFactory(
    private val storeId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PublicStoreViewModel(storeId) as T
}
