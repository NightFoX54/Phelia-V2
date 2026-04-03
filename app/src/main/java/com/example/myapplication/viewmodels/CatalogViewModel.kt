package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import com.example.myapplication.models.Category
import com.example.myapplication.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CatalogUiState(
    val featuredProducts: List<Product> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
)

class CatalogViewModel : ViewModel() {
    private val initialProducts = listOf(
        Product(
            id = "1",
            name = "Wireless Headphones Pro",
            price = 299.99,
            imageUrl = "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.8,
            category = "Audio",
        ),
        Product(
            id = "2",
            name = "Smart Watch Series 5",
            price = 399.99,
            imageUrl = "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.9,
            category = "Wearables",
        ),
        Product(
            id = "3",
            name = "Ultra Laptop Pro 15",
            price = 1299.99,
            imageUrl = "https://images.unsplash.com/photo-1759668358660-0d06064f0f84?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.7,
            category = "Computers",
        ),
        Product(
            id = "4",
            name = "Smartphone X12 Pro",
            price = 999.99,
            imageUrl = "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.6,
            category = "Phones",
        ),
        Product(
            id = "5",
            name = "Professional Camera Kit",
            price = 1899.99,
            imageUrl = "https://images.unsplash.com/photo-1729655669048-a667a0b01148?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.9,
            category = "Cameras",
        ),
        Product(
            id = "6",
            name = "Tablet Pro 12.9",
            price = 799.99,
            imageUrl = "https://images.unsplash.com/photo-1769603795371-ad63bd85d524?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            rating = 4.8,
            category = "Tablets",
        ),
    )

    private val _uiState = MutableStateFlow(
        CatalogUiState(
            featuredProducts = initialProducts.take(6),
            allProducts = initialProducts + listOf(
                initialProducts[0].copy(id = "7", name = "Mechanical Gaming Keyboard", price = 159.99, rating = 4.7, category = "Accessories"),
                initialProducts[0].copy(id = "8", name = "Wireless Gaming Mouse", price = 89.99, rating = 4.6, category = "Accessories"),
                initialProducts[0].copy(id = "9", name = "Portable Bluetooth Speaker", price = 129.99, rating = 4.5, category = "Audio"),
                initialProducts[0].copy(id = "10", name = "True Wireless Earbuds", price = 199.99, rating = 4.8, category = "Audio"),
                initialProducts[0].copy(id = "11", name = "Portable Power Bank 20K", price = 49.99, rating = 4.4, category = "Accessories"),
                initialProducts[0].copy(id = "12", name = "Premium Phone Case", price = 39.99, rating = 4.3, category = "Accessories"),
            ),
            categories = listOf(
                Category("All", isActive = true),
                Category("Phones"),
                Category("Computers"),
                Category("Audio"),
                Category("Wearables"),
                Category("Cameras"),
                Category("Tablets"),
                Category("Accessories"),
            ),
        ),
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    fun setActiveCategory(name: String) {
        _uiState.value = _uiState.value.copy(
            categories = _uiState.value.categories.map { it.copy(isActive = it.name == name) },
        )
    }

    fun findProduct(productId: String): Product? {
        return _uiState.value.allProducts.firstOrNull { it.id == productId }
    }
}

