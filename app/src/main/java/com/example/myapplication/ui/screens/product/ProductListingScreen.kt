package com.example.myapplication.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.components.ProductCard
import com.example.myapplication.ui.components.SearchField
import com.example.myapplication.viewmodel.CatalogViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel

@Composable
fun ProductListingScreen(
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenStore: (String) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val uiState by catalogViewModel.uiState.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()

    val (query, setQuery) = remember { mutableStateOf("") }
    val (selectedPriceRange, setSelectedPriceRange) = remember { mutableIntStateOf(0) }
    val (selectedSort, setSelectedSort) = remember { mutableStateOf("featured") }
    val (showFilters, setShowFilters) = remember { mutableStateOf(false) }

    val categories = uiState.categories.map { it.name }
    val priceRanges = listOf(
        "All Prices" to (0.0 to Double.POSITIVE_INFINITY),
        "Under $100" to (0.0 to 100.0),
        "$100 - $500" to (100.0 to 500.0),
        "$500 - $1000" to (500.0 to 1000.0),
        "Over $1000" to (1000.0 to Double.POSITIVE_INFINITY),
    )
    val sortOptions = listOf(
        "Featured" to "featured",
        "Price: Low to High" to "price-asc",
        "Price: High to Low" to "price-desc",
        "Rating: High to Low" to "rating",
    )

    val priceRange = priceRanges[selectedPriceRange].second
    val activeCategoryName = uiState.categories.firstOrNull { it.isActive }?.name ?: "All"
    var filtered = uiState.allProducts.filter { p ->
        val matchesSearch = p.name.contains(query, ignoreCase = true)
        val matchesCategory = activeCategoryName == "All" || p.category == activeCategoryName
        val matchesPrice = p.price >= priceRange.first && p.price <= priceRange.second
        matchesSearch && matchesCategory && matchesPrice
    }
    filtered = when (selectedSort) {
        "price-asc" -> filtered.sortedBy { it.price }
        "price-desc" -> filtered.sortedByDescending { it.price }
        "rating" -> filtered.sortedByDescending { it.rating }
        else -> filtered
    }

    val activeFiltersCount =
        (if (activeCategoryName != "All") 1 else 0) +
            (if (selectedPriceRange != 0) 1 else 0) +
            (if (selectedSort != "featured") 1 else 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppTopBar(title = "Products", onBack = onBack, containerColor = Color.White)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    SearchField(
                        value = query,
                        onValueChange = setQuery,
                        placeholder = "Search products...",
                        modifier = Modifier.weight(1f),
                    )

                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge { Text(activeFiltersCount.toString()) }
                            }
                        },
                    ) {
                        Button(
                            onClick = { setShowFilters(!showFilters) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(52.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }

        if (showFilters) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Filters", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (activeFiltersCount > 0) {
                            Text(
                                text = "Clear All",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Transparent),
                            )
                        }
                    }

                    Text("Category", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
                    FlowChips(
                        items = categories,
                        selected = activeCategoryName,
                        onSelect = { catalogViewModel.setActiveCategory(it) },
                    )

                    Text("Price Range", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
                    FlowChips(
                        items = priceRanges.map { it.first },
                        selectedIndex = selectedPriceRange,
                        onSelectIndex = setSelectedPriceRange,
                    )

                    Text("Sort By", fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
                    FlowChips(
                        items = sortOptions.map { it.first },
                        selected = sortOptions.firstOrNull { it.second == selectedSort }?.first ?: "Featured",
                        onSelect = { label ->
                            setSelectedSort(sortOptions.first { it.first == label }.second)
                        },
                    )
                }
            }
        }

        Text(
            text = "${filtered.size} " + if (filtered.size == 1) "product found" else "products found",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )

        val maxCurrentLineSpan = 2 // Since GridCells.Fixed(2)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
        ) {
            if (uiState.searchQuery.isNotEmpty()) {
                if (uiState.filteredStores.isNotEmpty()) {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Text(
                            text = "Stores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(uiState.filteredStores, span = { GridItemSpan(maxCurrentLineSpan) }) { store ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onOpenStore(store.storeId) },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (store.logo.isNotBlank()) {
                                        AsyncImage(model = store.logo, contentDescription = null)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = Color(0xFF4338CA)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(store.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        store.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Text(
                    text = if (uiState.searchQuery.isEmpty()) "All Products" else "Product Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(filtered, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onOpenProduct(product.id) },
                    isFavorite = favoriteIds.contains(product.id),
                    onFavoriteClick = { favoritesViewModel.toggleFavorite(product.id) },
                )
            }
            item { Spacer(modifier = Modifier.padding(bottom = 18.dp)) }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChips(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { label ->
            FilterChip(
                selected = selected == label,
                onClick = { onSelect(label) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChips(
    items: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelectIndex(index) },
                label = { Text(label) },
            )
        }
    }
}

