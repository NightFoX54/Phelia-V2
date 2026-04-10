package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.myapplication.ui.components.HorizontalBarEntry
import com.example.myapplication.ui.components.LineChartEntry
import com.example.myapplication.ui.components.ReadableHorizontalBarChart
import com.example.myapplication.ui.components.ReadableLineChart
import com.example.myapplication.viewmodel.AdminDashboardUiState
import com.example.myapplication.viewmodel.AdminDashboardViewModel
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    onManageUsers: () -> Unit,
    onManageStores: () -> Unit,
    onInactiveProducts: () -> Unit = {},
    onStoreApplications: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = viewModel(),
) {
    val headerGradient = Brush.linearGradient(listOf(Color(0xFF6D28D9), Color(0xFF4338CA)))
    val dashboardState by viewModel.uiState.collectAsState()
    val overview = (dashboardState as? AdminDashboardUiState.Ready)?.overview
    val ordersOverTime = overview?.ordersOverTime?.map { LineChartEntry(it.first, it.second) } ?: emptyList()
    val topSellingChart = overview?.topSellingProducts?.map { HorizontalBarEntry(it.first, it.second) } ?: emptyList()
    val topProducts = overview?.topProducts.orEmpty()
    val worstProducts = overview?.worstProducts.orEmpty()
    val topStores = overview?.topStores.orEmpty()

    LazyColumn(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(headerGradient)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Platform Analytics & Management", color = Color.White.copy(alpha = 0.85f))

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        Icons.Default.People,
                        "Total Users",
                        overview?.totalUsers?.toString() ?: "—",
                        "Registered accounts",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        Icons.Default.Storefront,
                        "Total Stores",
                        overview?.totalStores?.toString() ?: "—",
                        "Approved stores",
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        Icons.Default.TrendingUp,
                        "Total Products",
                        overview?.totalProducts?.toString() ?: "—",
                        "Across all stores",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        Icons.Default.ShoppingCart,
                        "Total Orders",
                        overview?.totalOrders?.toString() ?: "—",
                        "All-time orders",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Orders Over Time", fontWeight = FontWeight.Bold)
                    when (dashboardState) {
                        is AdminDashboardUiState.Loading -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Loading real data…", color = Color(0xFF6B7280))
                            }
                        }
                        is AdminDashboardUiState.Error -> {
                            val msg = (dashboardState as AdminDashboardUiState.Error).message
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(msg, color = Color(0xFFDC2626))
                            TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                        is AdminDashboardUiState.Ready -> {
                            if (ordersOverTime.size >= 2) {
                                ReadableLineChart(points = ordersOverTime, lineColor = Color(0xFF7C3AED), height = 260.dp)
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Not enough order data yet.", color = Color(0xFF6B7280))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Top Selling Products", fontWeight = FontWeight.Bold)
                    if (topSellingChart.isNotEmpty()) {
                        ReadableHorizontalBarChart(values = topSellingChart, barColor = Color(0xFF4338CA), height = 280.dp)
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Not enough data yet.", color = Color(0xFF6B7280))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Top Products", fontWeight = FontWeight.Bold)
                    }
                    if (topProducts.isEmpty()) {
                        Text("Not enough data yet.", color = Color(0xFF6B7280))
                    }
                    topProducts.forEachIndexed { idx, product ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("#${idx + 1}", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 36.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text("${formatCompactNumber(product.unitsSold)} sales", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCompactCurrency(product.revenue), color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Worst Performing Products", fontWeight = FontWeight.Bold)
                    }
                    if (worstProducts.isEmpty()) {
                        Text("Not enough data yet.", color = Color(0xFF6B7280))
                    }
                    worstProducts.forEach { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text("${formatCompactNumber(product.unitsSold)} sales", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCompactCurrency(product.revenue), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Top Stores", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    if (topStores.isEmpty()) {
                        Text("Not enough data yet.", color = Color(0xFF6B7280))
                    }
                    topStores.forEach { store ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(store.storeName, fontWeight = FontWeight.SemiBold)
                                Text("${formatCompactNumber(store.orderCount)} orders", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCompactCurrency(store.revenue), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onManageUsers,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Manage Users", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onManageStores,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Manage Stores", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStoreApplications,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.AppRegistration, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Store applications", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onInactiveProducts,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Manage inactive products", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    sub: String,
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
            Text(sub, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun formatCompactNumber(value: Int): String = when {
    value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
    value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000f)
    else -> value.toString()
}

private fun formatCompactCurrency(value: Double): String = when {
    value >= 1_000_000.0 -> "$" + String.format(Locale.US, "%.2fM", value / 1_000_000.0)
    value >= 1_000.0 -> "$" + String.format(Locale.US, "%.1fK", value / 1_000.0)
    else -> "$" + String.format(Locale.US, "%.0f", value)
}

