package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Store
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.components.CategoryChipsRow
import com.example.myapplication.ui.components.ProductCard
import com.example.myapplication.ui.components.SearchField
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.PublicStoreViewModel
import com.example.myapplication.viewmodel.PublicStoreViewModelFactory

@Composable
fun PublicStoreScreen(
    storeId: String,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier,
    viewModel: PublicStoreViewModel = viewModel(factory = PublicStoreViewModelFactory(storeId)),
    onOpenStore: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            AppTopBar(
                title = uiState.store?.name?.takeIf { it.isNotBlank() } ?: "Store",
                onBack = onBack,
            )
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.store == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Back") }
                }
            }
            else -> {
                val store = uiState.store
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        if (store != null) {
                            Column {
                                StoreProfileHeader(store = store)
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = "Search in this store...",
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CategoryChipsRow(
                                    categories = uiState.categories,
                                    onSelect = { viewModel.setActiveCategory(it) },
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    items(uiState.products, key = { it.id }) { p ->
                        ProductCard(
                            product = p,
                            onClick = { onOpenProduct(p.id) },
                            isFavorite = favoriteIds.contains(p.id),
                            onFavoriteClick = { favoritesViewModel.toggleFavorite(p.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreProfileHeader(
    store: Store,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (store.logo.isNotBlank()) {
                    AsyncImage(
                        model = store.logo,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                    Text(
                        store.name.ifBlank { "Store" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("★★★★★", color = Color(0xFFF59E0B), style = MaterialTheme.typography.bodySmall)
                        Text(
                            "  " + String.format("%.1f", store.rating),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            " (${store.reviewCount} reviews)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            if (store.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    store.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Products from this seller",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
