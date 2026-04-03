package com.example.myapplication.screens.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.components.CategoryChipsRow
import com.example.myapplication.components.ProductCard
import com.example.myapplication.components.SearchField
import com.example.myapplication.viewmodels.CatalogViewModel

@Composable
fun HomeScreen(
    onOpenCart: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val uiState by catalogViewModel.uiState.collectAsState()

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
                                text = "John Doe",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(
                            onClick = onOpenCart,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF3F4F6)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF374151),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SearchField(
                        value = "",
                        onValueChange = {},
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

        item {
            // Featured banner
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
                        "Up to 50% OFF",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "On selected items this week",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenProducts,
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

