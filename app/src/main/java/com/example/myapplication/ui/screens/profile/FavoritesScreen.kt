package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var onSaleOnly by remember { mutableStateOf(false) }
    var sortKey by remember { mutableStateOf("name") }

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

    val categories = remember(products) {
        products.mapNotNull { it.categoryName?.takeIf { c -> c.isNotBlank() } }.distinct().sorted()
    }

    val filteredProducts = remember(products, selectedCategory, onSaleOnly, sortKey) {
        var list = products
        selectedCategory?.let { cat -> list = list.filter { it.categoryName == cat } }
        if (onSaleOnly) list = list.filter { it.minDiscountPercent > 0 }
        when (sortKey) {
            "price-asc" -> list.sortedBy { it.minPrice }
            "price-desc" -> list.sortedByDescending { it.minPrice }
            "rating" -> list.sortedByDescending { it.rating }
            else -> list.sortedBy { it.name.lowercase() }
        }
    }

    val activeFilterCount =
        (if (selectedCategory != null) 1 else 0) +
            (if (onSaleOnly) 1 else 0) +
            (if (sortKey != "name") 1 else 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                AppTopBar(
                    title = "Favorites",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter favorites")
                        }
                    },
                )
                Text(
                    text = buildString {
                        append("${favoriteEntries.size} ")
                        append(if (favoriteEntries.size == 1) "item" else "items")
                        if (activeFilterCount > 0) {
                            append(" · ")
                            append(activeFilterCount)
                            append(if (activeFilterCount == 1) " filter" else " filters")
                        }
                    },
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
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
                    items(filteredProducts, key = { it.productId }) { summary ->
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

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter favorites") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") },
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text(cat) },
                            )
                        }
                    }
                    Text("Offers", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = !onSaleOnly,
                            onClick = { onSaleOnly = false },
                            label = { Text("All") },
                        )
                        FilterChip(
                            selected = onSaleOnly,
                            onClick = { onSaleOnly = true },
                            label = { Text("On sale") },
                        )
                    }
                    Text("Sort", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "name" to "Name",
                            "price-asc" to "Price ↑",
                            "price-desc" to "Price ↓",
                            "rating" to "Rating",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = sortKey == key,
                                onClick = { sortKey = key },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedCategory = null
                        onSaleOnly = false
                        sortKey = "name"
                    },
                ) {
                    Text("Reset")
                }
            },
        )
    }
}

private fun CatalogProductSummary.toUiProduct(): Product =
    Product(
        id = productId,
        name = name,
        price = minPrice,
        basePrice = minBasePrice,
        discountPercent = minDiscountPercent,
        imageUrl = imageUrl,
        rating = rating,
        reviewCount = reviewCount,
        category = categoryName,
        brandName = brandName,
    )
