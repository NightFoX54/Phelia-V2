package com.example.myapplication.ui.screens.store

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.OrderStatus
import com.example.myapplication.data.model.StoreSuborderListRow
import com.example.myapplication.data.model.normalizeOrderStatus
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.viewmodel.StoreOrdersLoadState
import com.example.myapplication.viewmodel.StoreOrdersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val orderFilters = listOf(
    "all" to "All",
    "received" to "Received",
    "preparing" to "Preparing",
    "shipped" to "Shipped",
    "completed" to "Done",
    "cancelled" to "Cancelled",
)

@Composable
fun StoreOrdersScreen(
    viewModel: StoreOrdersViewModel,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows by viewModel.rows.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOrdersIfPossible()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.refreshOrdersIfPossible()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filtered = remember(rows, searchQuery, selectedFilter) {
        rows.filter { row ->
            val q = searchQuery.trim()
            val matchSearch = q.isEmpty() ||
                row.orderId.contains(q, ignoreCase = true) ||
                row.buyerDisplayName.contains(q, ignoreCase = true)
            val s = normalizeOrderStatus(row.suborder.status)
            val matchFilter = when (selectedFilter) {
                "all" -> true
                "received" -> s == OrderStatus.ORDER_RECEIVED
                "preparing" -> s == OrderStatus.ORDER_CONFIRMED || s == OrderStatus.PREPARING
                "shipped" -> s == OrderStatus.SHIPPED
                "completed" -> s == OrderStatus.COMPLETED
                "cancelled" -> s == OrderStatus.CANCELLED
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    LazyColumn(
        modifier = modifier.background(Color(0xFFF9FAFB)),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED))))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Orders", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${rows.size} packages from your store", color = Color.White.copy(alpha = 0.85f))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by order id or customer...", color = Color(0xFFC7D2FE)) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    orderFilters.forEach { (key, label) ->
                        val count = when (key) {
                            "all" -> rows.size
                            "received" -> rows.count { normalizeOrderStatus(it.suborder.status) == OrderStatus.ORDER_RECEIVED }
                            "preparing" -> rows.count {
                                val s = normalizeOrderStatus(it.suborder.status)
                                s == OrderStatus.ORDER_CONFIRMED || s == OrderStatus.PREPARING
                            }
                            "shipped" -> rows.count { normalizeOrderStatus(it.suborder.status) == OrderStatus.SHIPPED }
                            "completed" -> rows.count { normalizeOrderStatus(it.suborder.status) == OrderStatus.COMPLETED }
                            "cancelled" -> rows.count { normalizeOrderStatus(it.suborder.status) == OrderStatus.CANCELLED }
                            else -> 0
                        }
                        FilterChipStore(
                            label = "$label ($count)",
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                        )
                    }
                }
            }
        }

        when (val st = loadState) {
            StoreOrdersLoadState.Idle, StoreOrdersLoadState.Loading -> {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            StoreOrdersLoadState.NoStore -> {
                item {
                    Text(
                        "No store linked to this account.",
                        modifier = Modifier.padding(24.dp),
                        color = Color(0xFFDC2626),
                    )
                }
            }
            is StoreOrdersLoadState.Error -> {
                item {
                    Text(
                        st.message,
                        modifier = Modifier.padding(24.dp),
                        color = Color(0xFFDC2626),
                    )
                }
            }
            StoreOrdersLoadState.Ready -> {
                if (filtered.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(999.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No orders yet", style = MaterialTheme.typography.titleMedium)
                            Text("Customer orders for your store will appear here.", color = Color(0xFF6B7280))
                        }
                    }
                } else {
                    items(filtered, key = { "${it.orderId}_${it.suborderFirestoreId}" }) { row ->
                        StoreOrderListCard(row = row, onClick = { onOpenOrder(row.orderId) })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StoreOrderListCard(
    row: StoreSuborderListRow,
    onClick: () -> Unit,
) {
    val dateStr = remember(row.orderCreatedAtMs) {
        if (row.orderCreatedAtMs <= 0L) "—"
        else SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(row.orderCreatedAtMs))
    }
    val orderLabel = remember(row.orderId) {
        val tail = row.orderId.takeLast(8).uppercase(Locale.US)
        "ORD-$tail"
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!row.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = row.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(orderLabel, fontWeight = FontWeight.Bold)
                Text(row.buyerDisplayName, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                SuborderStatusBadge(row.suborder.status)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Whole order: ${orderStatusLabelEnglish(row.parentOrderStatus)}",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${row.itemCount} item(s) · your total",
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    val t = row.suborder.totalPrice + row.suborder.totalTax
                    Text(
                        "$" + String.format(Locale.US, "%.2f", t),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(dateStr, color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SuborderStatusBadge(status: String) {
    val n = normalizeOrderStatus(status)
    val (label, bg, fg, icon) = when (n) {
        OrderStatus.ORDER_RECEIVED -> Quad("Received", Color(0xFFFEF9C3), Color(0xFFA16207), Icons.Default.Schedule)
        OrderStatus.ORDER_CONFIRMED, OrderStatus.PREPARING -> Quad("Preparing", Color(0xFFDBEAFE), Color(0xFF1D4ED8), Icons.Default.Inventory)
        OrderStatus.SHIPPED -> Quad("Shipped", Color(0xFFE0E7FF), Color(0xFF4338CA), Icons.Default.LocalShipping)
        OrderStatus.COMPLETED -> Quad("Completed", Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.CheckCircle)
        OrderStatus.CANCELLED -> Quad("Cancelled", Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.Close)
        else -> Quad(orderStatusLabelEnglish(status), Color(0xFFF3F4F6), Color(0xFF374151), Icons.Default.Inventory)
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

@Composable
private fun FilterChipStore(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
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

private data class Quad(
    val label: String,
    val bg: Color,
    val fg: Color,
    val icon: ImageVector,
)
