package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AppTopBar

private data class StoreProductItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val image: String,
    val views: Int,
    val sales: Int,
    val stock: Int,
)

@Composable
fun StoreProductsScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val products = remember {
        listOf(
            StoreProductItem("1", "Wireless Headphones Pro", 299.99, "Electronics", "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 1243, 89, 45),
            StoreProductItem("2", "Smart Watch Series 5", 399.99, "Electronics", "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 2156, 134, 23),
            StoreProductItem("3", "Laptop Pro 15", 1299.99, "Electronics", "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 876, 45, 12),
            StoreProductItem("4", "Smartphone X12 Pro", 999.99, "Electronics", "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 3421, 201, 8),
        )
    }
    val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Box(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
    ) {
        LazyColumn {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED))))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Products", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("${products.size} products", color = Color.White.copy(alpha = 0.85f))
                        }
                        Button(
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Add")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFC7D2FE)) },
                        trailingIcon = { IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null, tint = Color.White) } },
                        placeholder = { Text("Search products...", color = Color(0xFFC7D2FE)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedBorderColor = Color.White.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products found", style = MaterialTheme.typography.titleMedium)
                        Text("Try a different search term", color = Color(0xFF6B7280))
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AsyncImage(model = product.image, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Badge(product.category)
                                        }
                                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color(0xFF9CA3AF)) }
                                    }
                                    Text("$" + String.format("%.2f", product.price), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MiniStat("${product.views}", "Views", Modifier.weight(1f), Color(0xFFEFF6FF), Color(0xFF2563EB))
                                        MiniStat("${product.sales}", "Sales", Modifier.weight(1f), Color(0xFFF0FDF4), Color(0xFF16A34A))
                                        MiniStat("${product.stock}", "Stock", Modifier.weight(1f), Color(0xFFFFF7ED), Color(0xFFEA580C))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onEditProduct(product.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                                ) { Icon(Icons.Default.Edit, null); Spacer(modifier = Modifier.size(6.dp)); Text("Edit") }
                                Button(
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                                ) { Icon(Icons.Default.Delete, null); Spacer(modifier = Modifier.size(6.dp)); Text("Delete") }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
        FloatingActionButton(
            onClick = onAddProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
        ) { Icon(Icons.Default.Add, null) }
    }
}

@Composable
private fun Badge(text: String) {
    Box(modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(text, color = Color(0xFF4338CA), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier, bg: Color, fg: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(value, color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
        }
    }
}

