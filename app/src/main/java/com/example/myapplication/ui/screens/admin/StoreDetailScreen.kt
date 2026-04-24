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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.ui.unit.sp
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
    val tabs = listOf("overview", "products", "orders")

    Column(modifier = modifier.background(Color(0xFFF8FAFC))) {
        // Professional Header with Gradient and Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
        ) {
            Column {
                AppTopBar(
                    title = "Store Dashboard",
                    onBack = onBack,
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (store.logo.isNotEmpty()) {
                            AsyncImage(
                                model = store.logo,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Icon(Icons.Default.Storefront, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = store.name,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Owner: ${store.ownerName}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text(
                                text = " ${String.format("%.1f", store.rating)}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF34D399),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Quick Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeaderStat("Total Sales", store.totalSalesLabel(), Modifier.weight(1f))
                    HeaderStat("Products", store.totalProducts.toString(), Modifier.weight(1f))
                    HeaderStat("Orders", store.totalOrders.toString(), Modifier.weight(1f))
                }
                
                // Tabs
                TabRow(
                    selectedTabIndex = tabs.indexOf(activeTab),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(activeTab)]),
                            color = Color(0xFF6366F1),
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            text = {
                                Text(
                                    tab.replaceFirstChar { it.uppercase() },
                                    fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp)
        ) {
            if (activeTab == "overview") {
                item {
                    InfoCard(
                        title = "General Information",
                        icon = Icons.Default.Storefront,
                        rows = listOf(
                            "Store Name" to store.name,
                            "Store ID" to store.storeId,
                            "Join Date" to store.joinDate,
                            "Description" to store.description
                        )
                    )
                }
                item {
                    InfoCard(
                        title = "Business & Legal",
                        icon = Icons.Default.Business,
                        rows = listOf(
                            "Business Email" to store.email,
                            "Phone Number" to store.phone,
                            "Tax Number" to store.taxNumber,
                            "Address" to store.businessAddress,
                            "Owner Email" to store.ownerEmail
                        )
                    )
                }
                item {
                    ChartCard("Revenue Analytics", Icons.Default.TrendingUp, Color(0xFF6366F1)) {
                        ReadableLineChart(
                            points = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun").mapIndexed { idx, month ->
                                LineChartEntry(month, store.monthlyRevenue[idx])
                            },
                            lineColor = Color(0xFF6366F1),
                            height = 200.dp,
                        )
                    }
                }
                item {
                    ChartCard("Inventory Distribution", Icons.Default.Inventory, Color(0xFF8B5CF6)) {
                        if (store.categoryLabels.isEmpty() || store.categoryDistribution.isEmpty()) {
                            Text("Data unavailable", color = Color(0xFF94A3B8), modifier = Modifier.padding(20.dp))
                        } else {
                            ReadableVerticalBarChart(
                                values = store.categoryLabels.mapIndexed { idx, label ->
                                    VerticalBarEntry(label, store.categoryDistribution.getOrElse(idx) { 0f })
                                },
                                barColor = Color(0xFF8B5CF6),
                                height = 200.dp,
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Top Performing Products", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            store.topProducts.forEachIndexed { idx, product ->
                                ProductListItem(idx + 1, product.first, product.second, product.third)
                                if (idx < store.topProducts.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Recent Transactions", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            store.recentOrders.forEachIndexed { idx, order ->
                                OrderListItem(order.first, order.second, order.third)
                                if (idx < store.recentOrders.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
                item {
                    ChartCard("Key Metrics", Icons.Default.TrendingUp, Color(0xFF6366F1)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatRow("Total Revenue", store.totalSalesLabel())
                            StatRow("Order Volume", store.totalOrders.toString())
                            StatRow(
                                "Avg. Basket Size",
                                "$" + String.format(Locale.US, "%.2f", store.totalSales / store.totalOrders.coerceAtLeast(1))
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rows: List<Pair<String, String>>,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            rows.forEachIndexed { idx, (label, value) ->
                Column {
                    Text(
                        text = label.uppercase(),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = value,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (idx < rows.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(rank: Int, name: String, category: String, price: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = rank.toString(), fontWeight = FontWeight.Bold, color = Color(0xFF475569), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(category, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }
        Text(price, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
    }
}

@Composable
private fun OrderListItem(orderId: String, date: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(orderId, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(date, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }
        Text(amount, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF475569), fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
    }
}

private fun com.example.myapplication.data.repository.AdminStoreDetail.totalSalesLabel(): String = when {
    totalSales >= 1_000_000.0 -> "$" + String.format(Locale.US, "%.2fM", totalSales / 1_000_000.0)
    totalSales >= 1_000.0 -> "$" + String.format(Locale.US, "%.1fK", totalSales / 1_000.0)
    else -> "$" + String.format(Locale.US, "%.0f", totalSales)
}

