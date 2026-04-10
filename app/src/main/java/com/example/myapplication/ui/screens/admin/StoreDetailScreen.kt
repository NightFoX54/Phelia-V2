package com.example.myapplication.ui.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.components.LineChartEntry
import com.example.myapplication.ui.components.ReadableLineChart
import com.example.myapplication.ui.components.ReadableVerticalBarChart
import com.example.myapplication.ui.components.VerticalBarEntry
import com.example.myapplication.viewmodel.AdminStoreDetailUiState
import com.example.myapplication.viewmodel.AdminStoreDetailViewModel
import com.example.myapplication.viewmodel.AdminStoreDetailViewModelFactory
import java.util.Locale

@Composable
fun StoreDetailScreen(
    storeId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: AdminStoreDetailViewModel = viewModel(factory = AdminStoreDetailViewModelFactory(storeId))
    val uiState by vm.uiState.collectAsState()
    var activeTab by remember { mutableStateOf("overview") }
    if (uiState is AdminStoreDetailUiState.Loading) {
        Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                AppTopBar(title = "Store Details", onBack = onBack, containerColor = Color.White)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                horizontalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        }
        return
    }
    if (uiState is AdminStoreDetailUiState.Error) {
        val msg = (uiState as AdminStoreDetailUiState.Error).message
        Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
            Surface(color = Color.White, shadowElevation = 1.dp) { AppTopBar(title = "Store Details", onBack = onBack, containerColor = Color.White) }
            Text(msg, modifier = Modifier.padding(20.dp), color = Color(0xFFDC2626))
        }
        return
    }
    val store = (uiState as AdminStoreDetailUiState.Ready).detail

    LazyColumn(
        modifier = modifier.background(Color(0xFFF9FAFB)),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF6D28D9), Color(0xFF4338CA))))
                    .padding(bottom = 18.dp),
            ) {
                AppTopBar(title = "Store Details", onBack = onBack, containerColor = Color.Transparent)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row {
                            Box(
                                modifier = Modifier.size(56.dp).background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                            ) {
                                Icon(Icons.Default.Storefront, null, tint = Color(0xFF4338CA))
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(store.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(store.ownerName, color = Color.White.copy(alpha = 0.85f))
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    repeat(5) { idx ->
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            tint = if (idx < store.rating.toInt()) Color(0xFFFDE047) else Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(String.format("%.1f", store.rating), color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                }
                            }
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(store.description, color = Color.White.copy(alpha = 0.9f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            HeaderStat("Products", store.totalProducts.toString(), Modifier.weight(1f))
                            HeaderStat("Orders", store.totalOrders.toString(), Modifier.weight(1f))
                            HeaderStat("Sales", store.totalSalesLabel(), Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                listOf("overview", "products", "orders").forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }

        if (activeTab == "overview") {
            item {
                InfoCard(
                    title = "Contact Information",
                    rows = listOf(
                        Triple(Icons.Default.Mail, "Owner Email", store.ownerEmail),
                        Triple(Icons.Default.Storefront, "Store ID", store.storeId),
                        Triple(Icons.Default.CalendarToday, "Join Date", store.joinDate),
                    ),
                )
            }
            item {
                ChartCard("Monthly Revenue", Icons.Default.TrendingUp, Color(0xFF16A34A)) {
                    ReadableLineChart(
                        points = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun").mapIndexed { idx, month ->
                            LineChartEntry(month, store.monthlyRevenue[idx])
                        },
                        lineColor = Color(0xFF10B981),
                        height = 240.dp,
                    )
                }
            }
            item {
                ChartCard("Product Categories", Icons.Default.Inventory, Color(0xFF4338CA)) {
                    if (store.categoryLabels.isEmpty() || store.categoryDistribution.isEmpty()) {
                        Text("Not enough category data yet.", color = Color(0xFF6B7280))
                    } else {
                        ReadableVerticalBarChart(
                            values = store.categoryLabels.mapIndexed { idx, label ->
                                VerticalBarEntry(label, store.categoryDistribution.getOrElse(idx) { 0f })
                            },
                            barColor = Color(0xFF4338CA),
                            height = 230.dp,
                        )
                    }
                }
            }
        }

        if (activeTab == "products") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Top Selling Products", fontWeight = FontWeight.Bold)
                        store.topProducts.forEachIndexed { idx, product ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp)).padding(10.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Text("#${idx + 1}", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.size(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.first, fontWeight = FontWeight.SemiBold)
                                    Text(product.second, color = Color(0xFF6B7280), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                }
                                Text(product.third, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (activeTab == "orders") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recent Orders", fontWeight = FontWeight.Bold)
                        store.recentOrders.forEach { order ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp)).padding(10.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.first, fontWeight = FontWeight.SemiBold)
                                    Text(order.second, color = Color(0xFF6B7280), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                }
                                Text(order.third, color = Color(0xFF4338CA), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item {
                ChartCard("Order Statistics", Icons.Default.ShoppingCart, Color(0xFF4338CA)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatRow("Total Orders", store.totalOrders.toString())
                        StatRow("Total Revenue", store.totalSalesLabel())
                        StatRow(
                            "Average Order Value",
                            "$" + String.format(
                                Locale.US,
                                "%.2f",
                                store.totalSales / store.totalOrders.coerceAtLeast(1),
                            ),
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)), modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(label, color = Color.White.copy(alpha = 0.85f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    rows: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String>>,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            rows.forEach { (icon, label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                    Column {
                        Text(label, color = Color(0xFF6B7280), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text(value)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor)
                Spacer(modifier = Modifier.size(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp)).padding(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
    }
}

private fun com.example.myapplication.data.repository.AdminStoreDetail.totalSalesLabel(): String = when {
    totalSales >= 1_000_000.0 -> "$" + String.format(Locale.US, "%.2fM", totalSales / 1_000_000.0)
    totalSales >= 1_000.0 -> "$" + String.format(Locale.US, "%.1fK", totalSales / 1_000.0)
    else -> "$" + String.format(Locale.US, "%.0f", totalSales)
}

