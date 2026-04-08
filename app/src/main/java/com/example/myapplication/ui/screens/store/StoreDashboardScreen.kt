package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.ui.components.SimpleBarChart
import com.example.myapplication.viewmodel.StoreProductsLoadState
import com.example.myapplication.viewmodel.StoreProductsViewModel
import java.util.Locale

@Composable
fun StoreDashboardScreen(
    storeProductsViewModel: StoreProductsViewModel,
    onAddProduct: () -> Unit,
    onOpenProductDetail: (String) -> Unit,
    onEditProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerGradient = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED)))
    val salesData = remember { listOf(12f, 19f, 15f, 25f, 22f, 30f, 28f) }
    val rows by storeProductsViewModel.rows.collectAsState()
    val loadState by storeProductsViewModel.loadState.collectAsState()
    val loadSt = loadState

    val totalStock = rows.sumOf { it.totalStock }
    val totalReviews = rows.sumOf { it.reviewCount }
    val totalVariants = rows.sumOf { it.variantCount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(headerGradient)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Store", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Store Dashboard", color = Color.White.copy(alpha = 0.85f))
                        }
                        Button(
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Add", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Default.Inventory, "Products", rows.size.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.TrendingUp, "In stock", totalStock.toString(), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Default.Star, "Reviews", totalReviews.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.Inventory, "Variants", totalVariants.toString(), modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Sales This Week", fontWeight = FontWeight.Bold)
                        Text("Placeholder chart — connect analytics when ready.", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                        SimpleBarChart(values = salesData, barColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Products", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${rows.size} items", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (loadSt) {
                StoreProductsLoadState.Idle, StoreProductsLoadState.Loading -> {
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
                StoreProductsLoadState.NoStore -> {
                    item {
                        Text(
                            "No store linked to this account.",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                is StoreProductsLoadState.Error -> {
                    item {
                        Text(
                            loadSt.message,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                StoreProductsLoadState.Ready -> {
                    if (rows.isEmpty()) {
                        item {
                            Text(
                                "No products yet. Tap Add to create one.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                color = Color(0xFF6B7280),
                            )
                        }
                    } else {
                        items(rows, key = { it.productId }) { product ->
                            DashboardProductCard(
                                product = product,
                                onOpenDetail = { onOpenProductDetail(product.productId) },
                                onEditProduct = onEditProduct,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        FloatingActionButton(
            onClick = onAddProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
private fun DashboardProductCard(
    product: StoreOwnerProductRow,
    onOpenDetail: () -> Unit,
    onEditProduct: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable(onClick = onOpenDetail),
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            if (!product.isActive) {
                                Text(
                                    "PASİF",
                                    color = Color(0xFFDC2626),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF)) }
                    }
                    Text(
                        "$" + String.format(Locale.US, "%.2f", product.minPrice),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("${product.reviewCount}", "Reviews", Color(0xFFEFF6FF), Color(0xFF2563EB), Modifier.weight(1f))
                        MiniStat("${product.totalStock}", "Stock", Color(0xFFFFF7ED), Color(0xFFEA580C), Modifier.weight(1f))
                        MiniStat("${product.variantCount}", "SKU", Color(0xFFF0FDF4), Color(0xFF16A34A), Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEditProduct(product.productId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Edit")
                }
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(value, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(label, color = fg.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
        }
    }
}
