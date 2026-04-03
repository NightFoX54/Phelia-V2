package com.example.myapplication.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.components.SimpleBarChart

private data class StoreProduct(
    val id: String,
    val name: String,
    val price: Double,
    val image: String,
    val views: Int,
    val sales: Int,
    val stock: Int,
)

@Composable
fun StoreDashboardScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerGradient = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED)))
    val salesData = listOf(12f, 19f, 15f, 25f, 22f, 30f, 28f)
    val products = listOf(
        StoreProduct("1", "Wireless Headphones Pro", 299.99, "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 1243, 89, 45),
        StoreProduct("2", "Smart Watch Series 5", 399.99, "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 2156, 134, 23),
        StoreProduct("3", "Laptop Pro 15", 1299.99, "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 876, 45, 12),
        StoreProduct("4", "Smartphone X12 Pro", 999.99, "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", 3421, 201, 8),
    )
    val totalViews = products.sumOf { it.views }
    val totalSales = products.sumOf { it.sales }
    val totalOrders = 151
    val revenue = products.sumOf { it.price * it.sales }

    Box(
        modifier = modifier
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
                        StatCard(Icons.Default.Visibility, "Total Views", totalViews.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.MonetizationOn, "Total Sales", totalSales.toString(), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Default.ShoppingCart, "Total Orders", totalOrders.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.TrendingUp, "Revenue", "$" + String.format("%.1fk", revenue / 1000), modifier = Modifier.weight(1f))
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
                        SimpleBarChart(values = salesData, barColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Products", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${products.size} items", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(products, key = { it.id }) { product ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(
                                model = product.image,
                                contentDescription = product.name,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(product.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF)) }
                                }
                                Text("$" + String.format("%.2f", product.price), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MiniStat("${product.views}", "Views", Color(0xFFEFF6FF), Color(0xFF2563EB), Modifier.weight(1f))
                                    MiniStat("${product.sales}", "Sales", Color(0xFFF0FDF4), Color(0xFF16A34A), Modifier.weight(1f))
                                    MiniStat("${product.stock}", "Stock", Color(0xFFFFF7ED), Color(0xFFEA580C), Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onEditProduct(product.id) },
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

