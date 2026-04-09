package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.CatalogProductSummary
import com.example.myapplication.data.model.ui.Product
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.components.ProductCard
import com.example.myapplication.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favoriteEntries by favoritesViewModel.favoriteEntries.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()

    val repository = remember { ProductRepository() }
    var products by remember { mutableStateOf<List<CatalogProductSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(favoriteEntries) {
        val ids = favoriteEntries.map { it.productId }
        if (ids.isEmpty()) {
            products = emptyList()
            loadError = null
            loading = false
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        repository.fetchCatalogSummariesForProductIds(ids).fold(
            onSuccess = {
                products = it
                loading = false
            },
            onFailure = { e ->
                loadError = e.message ?: "Could not load products"
                loading = false
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppTopBar(title = "Favorites", onBack = onBack, containerColor = Color.White)
                Text(
                    text = "${favoriteEntries.size} " + if (favoriteEntries.size == 1) "item" else "items",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        when {
            loading && products.isEmpty() && favoriteEntries.isNotEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            loadError != null -> {
                Text(
                    text = loadError!!,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(20.dp),
                )
            }
            favoriteEntries.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No favorites yet.", color = Color(0xFF6B7280))
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxSize(),
                ) {
                    items(products, key = { it.productId }) { summary ->
                        val ui = summary.toUiProduct()
                        ProductCard(
                            product = ui,
                            onClick = { onOpenProduct(ui.id) },
                            isFavorite = favoriteIds.contains(ui.id),
                            onFavoriteClick = { favoritesViewModel.toggleFavorite(ui.id) },
                        )
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
                }
            }
        }
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
