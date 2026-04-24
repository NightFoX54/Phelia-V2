package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.CategoryChipsRow
import com.example.myapplication.ui.components.ProductCard
import com.example.myapplication.ui.components.SearchField
import com.example.myapplication.viewmodel.CatalogViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel

@Composable
fun HomeScreen(
    userName: String,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenProducts: () -> Unit,
    /** Opens catalog filtered to products that have a discount. */
    onOpenSaleProducts: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenStore: (String) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val uiState by catalogViewModel.uiState.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()
    val onSaleProducts = uiState.allProducts.filter { it.discountPercent > 0 }
    val hasSpecialOffers = onSaleProducts.isNotEmpty()
    val maxSalePercent = onSaleProducts.maxOfOrNull { it.discountPercent } ?: 0

    LazyColumn(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            // Header
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                            )
                            Text(
                                text = userName.ifBlank { "Guest" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onOpenMessages,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFF3F4F6)),
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadMessagesCount > 0) {
                                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                                Text(unreadMessagesCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Messages",
                                        tint = Color(0xFF374151),
                                    )
                                }
                            }

                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFF3F4F6)),
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                                Text(unreadNotificationsCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color(0xFF374151),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SearchField(
                        value = uiState.searchQuery,
                        onValueChange = { catalogViewModel.setSearchQuery(it) },
                        placeholder = "Search products...",
                    )
                }
            }
        }

        item {
            // Categories
            Surface(color = Color.White) {
                CategoryChipsRow(
                    categories = uiState.categories,
                    onSelect = { catalogViewModel.setActiveCategory(it) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }

        if (hasSpecialOffers) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF4338CA), Color(0xFF7C3AED)),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(gradient)
                            .padding(18.dp),
                    ) {
                        Text("Special Offer", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Up to $maxSalePercent% OFF",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        val count = onSaleProducts.size
                        Text(
                            if (count == 1) "1 product on sale right now" else "$count products on sale right now",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenSaleProducts,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF4338CA),
                            ),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text("Shop Now", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            // Products header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Featured Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "See All",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onOpenProducts() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (uiState.searchQuery.isNotEmpty()) {
            if (uiState.filteredStores.isNotEmpty()) {
                item {
                    Text(
                        text = "Stores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                items(uiState.filteredStores) { store ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clickable { onOpenStore(store.storeId) }, // Correct navigation to store
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
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF4338CA))
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
            } else if (uiState.featuredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No stores or products found", color = Color.Gray)
                    }
                }
            }
        }

        items(uiState.featuredProducts.chunked(2)) { rowProducts ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                rowProducts.forEach { product ->
                    Box(modifier = Modifier.weight(1f)) {
                        ProductCard(
                            product = product,
                            onClick = { onOpenProduct(product.id) },
                            isFavorite = favoriteIds.contains(product.id),
                            onFavoriteClick = { favoritesViewModel.toggleFavorite(product.id) },
                        )
                    }
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

