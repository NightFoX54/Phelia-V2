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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

private data class StoreOrder(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val items: Int,
    val total: Double,
    val status: String,
    val date: String,
    val productImage: String,
)

@Composable
fun StoreOrdersScreen(
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    val orders = remember {
        listOf(
            StoreOrder("1", "ORD-2024-001", "John Doe", 2, 699.98, "processing", "Mar 20, 2024", "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
            StoreOrder("2", "ORD-2024-002", "Sarah Smith", 1, 1299.99, "completed", "Mar 19, 2024", "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
            StoreOrder("3", "ORD-2024-003", "Mike Johnson", 1, 399.99, "pending", "Mar 21, 2024", "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
            StoreOrder("4", "ORD-2024-004", "Emily Davis", 3, 2599.97, "processing", "Mar 21, 2024", "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
            StoreOrder("5", "ORD-2024-005", "Tom Wilson", 1, 999.99, "cancelled", "Mar 18, 2024", "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
            StoreOrder("6", "ORD-2024-006", "Lisa Anderson", 2, 1699.98, "completed", "Mar 17, 2024", "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"),
        )
    }
    val filters = listOf("all", "pending", "processing", "completed", "cancelled")
    val filtered = orders.filter {
        val matchSearch =
            it.orderNumber.contains(searchQuery, true) || it.customerName.contains(searchQuery, true)
        val matchFilter = selectedFilter == "all" || it.status == selectedFilter
        matchSearch && matchFilter
    }

    LazyColumn(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED))))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Orders", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${orders.size} total orders", color = Color.White.copy(alpha = 0.85f))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search orders...", color = Color(0xFFC7D2FE)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFC7D2FE)) },
                    trailingIcon = { Icon(Icons.Default.FilterList, null, tint = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { filter ->
                        val count = if (filter == "all") orders.size else orders.count { it.status == filter }
                        FilterChip(
                            label = "${filter.replaceFirstChar { it.uppercase() }} ($count)",
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No orders found", style = MaterialTheme.typography.titleMedium)
                    Text("Try a different search/filter", color = Color(0xFF6B7280))
                }
            }
        } else {
            items(filtered, key = { it.id }) { order ->
                Card(
                    onClick = { onOpenOrder(order.id) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(model = order.productImage, contentDescription = null, modifier = Modifier.size(80.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.orderNumber, fontWeight = FontWeight.Bold)
                            Text(order.customerName, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            StatusBadge(order.status)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${order.items} item${if (order.items > 1) "s" else ""}", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("$" + String.format("%.2f", order.total), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text(order.date, color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Color.White else Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF4338CA) else Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, bg, fg, icon) = when (status) {
        "pending" -> Quad("Pending", Color(0xFFFEF9C3), Color(0xFFA16207), Icons.Default.Schedule)
        "processing" -> Quad("Processing", Color(0xFFDBEAFE), Color(0xFF1D4ED8), Icons.Default.Inventory)
        "completed" -> Quad("Completed", Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.CheckCircle)
        else -> Quad("Cancelled", Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.Close)
    }
    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Text(label, color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private data class Quad(
    val a: String,
    val b: Color,
    val c: Color,
    val d: androidx.compose.ui.graphics.vector.ImageVector,
)

