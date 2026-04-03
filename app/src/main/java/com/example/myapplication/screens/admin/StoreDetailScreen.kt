package com.example.myapplication.screens.admin

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.myapplication.components.AppTopBar
import com.example.myapplication.components.LineChartEntry
import com.example.myapplication.components.ReadableLineChart
import com.example.myapplication.components.ReadableVerticalBarChart
import com.example.myapplication.components.VerticalBarEntry

private data class StoreDetailsUi(
    val id: String,
    val name: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val rating: Double,
    val totalSales: String,
    val totalProducts: Int,
    val totalOrders: Int,
    val status: String,
    val joinDate: String,
    val description: String,
    val address: String,
    val website: String,
    val businessHours: String,
    val monthlyRevenue: List<Float>,
    val categoryDistribution: List<Float>,
    val categoryLabels: List<String>,
    val topProducts: List<Triple<String, String, String>>,
    val recentOrders: List<Triple<String, String, String>>,
)

@Composable
fun StoreDetailScreen(
    storeId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val store = remember(storeId) { mockStoreDetails(storeId) }
    var activeTab by remember { mutableStateOf("overview") }

    if (store == null) {
        Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                AppTopBar(title = "Store Not Found", onBack = onBack, containerColor = Color.White)
            }
            Text("The requested store does not exist.", modifier = Modifier.padding(20.dp))
        }
        return
    }

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
                            if (store.status == "active") {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(store.description, color = Color.White.copy(alpha = 0.9f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            HeaderStat("Products", store.totalProducts.toString(), Modifier.weight(1f))
                            HeaderStat("Orders", store.totalOrders.toString(), Modifier.weight(1f))
                            HeaderStat("Sales", store.totalSales, Modifier.weight(1f))
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
                        Triple(Icons.Default.Phone, "Phone", store.ownerPhone),
                        Triple(Icons.Default.LocationOn, "Address", store.address),
                        Triple(Icons.Default.Language, "Website", store.website),
                        Triple(Icons.Default.Schedule, "Business Hours", store.businessHours),
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
                    ReadableVerticalBarChart(
                        values = store.categoryLabels.mapIndexed { idx, label ->
                            VerticalBarEntry(label, store.categoryDistribution[idx])
                        },
                        barColor = Color(0xFF4338CA),
                        height = 230.dp,
                    )
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
                        StatRow("Total Revenue", store.totalSales)
                        StatRow("Average Order Value", "$${(store.totalSales.filter { it.isDigit() }.toLongOrNull() ?: 0L) / store.totalOrders.coerceAtLeast(1)}")
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

private fun mockStoreDetails(id: String): StoreDetailsUi? {
    val map = mapOf(
        "1" to StoreDetailsUi(
            id = "1",
            name = "TechStore Pro",
            ownerName = "Store Owner",
            ownerEmail = "store@test.com",
            ownerPhone = "+1 (555) 123-4567",
            rating = 4.9,
            totalSales = "$2,548,000",
            totalProducts = 145,
            totalOrders = 1250,
            status = "active",
            joinDate = "2023-06-15",
            description = "Premium technology store offering the latest gadgets and electronics from top brands.",
            address = "123 Tech Street, Silicon Valley, CA 94025",
            website = "www.techstorepro.com",
            businessHours = "Mon-Fri: 9AM-6PM, Sat: 10AM-4PM",
            monthlyRevenue = listOf(180f, 220f, 195f, 245f, 210f, 280f),
            categoryDistribution = listOf(45f, 35f, 40f, 25f),
            categoryLabels = listOf("Phones", "Laptops", "Acc.", "Tablets"),
            topProducts = listOf(
                Triple("iPhone 15 Pro Max", "234 sales", "$234,000"),
                Triple("MacBook Pro M3", "156 sales", "$312,000"),
                Triple("AirPods Pro 2", "189 sales", "$47,250"),
            ),
            recentOrders = listOf(
                Triple("ORD-1234", "John Doe - 2024-03-20", "$1,299"),
                Triple("ORD-1235", "Jane Smith - 2024-03-20", "$899"),
                Triple("ORD-1236", "Mike Johnson - 2024-03-19", "$2,199"),
            ),
        ),
        "2" to StoreDetailsUi(
            id = "2",
            name = "Gadget Hub",
            ownerName = "Mike Anderson",
            ownerEmail = "mike@store.com",
            ownerPhone = "+1 (555) 234-5678",
            rating = 4.8,
            totalSales = "$1,823,500",
            totalProducts = 98,
            totalOrders = 890,
            status = "active",
            joinDate = "2023-08-20",
            description = "One-stop shop for innovative gadgets and smart home devices.",
            address = "456 Innovation Blvd, Austin, TX 78701",
            website = "www.gadgethub.com",
            businessHours = "Mon-Sat: 10AM-7PM, Sun: 11AM-5PM",
            monthlyRevenue = listOf(145f, 165f, 178f, 189f, 195f, 210f),
            categoryDistribution = listOf(28f, 22f, 30f, 18f),
            categoryLabels = listOf("Home", "Wear", "Audio", "Game"),
            topProducts = listOf(
                Triple("Samsung Galaxy Watch", "178 sales", "$71,200"),
                Triple("Google Nest Hub", "145 sales", "$21,750"),
                Triple("Sony WH-1000XM5", "123 sales", "$49,200"),
            ),
            recentOrders = listOf(
                Triple("ORD-2234", "Sarah Lee - 2024-03-20", "$399"),
                Triple("ORD-2235", "Tom Brown - 2024-03-19", "$599"),
                Triple("ORD-2236", "Lisa White - 2024-03-19", "$249"),
            ),
        ),
    )
    return map[id] ?: map["1"]
}

