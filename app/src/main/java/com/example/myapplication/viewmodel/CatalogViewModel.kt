package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.CatalogProductSummary
import com.example.myapplication.data.model.ui.Category
import com.example.myapplication.data.model.ui.Product
import com.example.myapplication.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val featuredProducts: List<Product> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
)

class CatalogViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState(isLoading = true))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val productsResult = productRepository.fetchCatalogSummaries()
            val categoriesResult = productRepository.fetchCategories()
            if (productsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = productsResult.exceptionOrNull()?.message ?: "Urunler yuklenemedi",
                    )
                }
                return@launch
            }
            val summaries = productsResult.getOrNull().orEmpty()
            val uiProducts = summaries.map { it.toUiProduct() }.sortedBy { it.name.lowercase() }
            val fireCategories = categoriesResult.getOrNull().orEmpty().sortedBy { it.name.lowercase() }
            val chips = listOf(Category(name = "All", isActive = true)) +
                fireCategories.map { Category(name = it.name, isActive = false) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = categoriesResult.exceptionOrNull()?.let { e -> "Kategoriler: ${e.message}" },
                    featuredProducts = uiProducts.take(6),
                    allProducts = uiProducts,
                    categories = chips,
                )
            }
        }
    }

    fun setActiveCategory(name: String) {
        _uiState.update { state ->
            state.copy(
                categories = state.categories.map { it.copy(isActive = it.name == name) },
            )
        }
    }

    private fun CatalogProductSummary.toUiProduct(): Product =
        Product(
            id = productId,
            name = name,
            price = minPrice,
            imageUrl = imageUrl,
            rating = rating,
            reviewCount = reviewCount,
            category = categoryName,
            brandName = brandName,
        )
}
