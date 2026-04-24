package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.myapplication.data.model.ChatThread
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
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenChat: (storeId: String, suborderId: String) -> Unit,
    initialTab: Int = 0,
) {
    val rows by viewModel.rows.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val chats by viewModel.chats.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(initialTab) } // 0: Orders, 1: Messages

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

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED))))
                .padding(top = topPadding)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Management", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Orders (${rows.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val unreadCount = remember(chats) {
                            val uid = viewModel.getCurrentUserId()
                            chats.count { chat ->
                                val lastRead = chat.lastReadBy[uid]
                                lastRead == null || lastRead.seconds < chat.lastMessageTimestamp.seconds
                            }
                        }
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = Color.Red) { Text("$unreadCount", color = Color.White) }
                            }
                        }) {
                            Text("Messages", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Orders Tab
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by ID or customer...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            orderFilters.take(3).forEach { (key, label) ->
                                FilterChipStore(
                                    label = label,
                                    selected = selectedFilter == key,
                                    onClick = { selectedFilter = key },
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            orderFilters.drop(3).forEach { (key, label) ->
                                FilterChipStore(
                                    label = label,
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
                            Text("No store linked.", modifier = Modifier.padding(24.dp), color = Color.Red)
                        }
                    }
                    is StoreOrdersLoadState.Error -> {
                        item {
                            Text(st.message, modifier = Modifier.padding(24.dp), color = Color.Red)
                        }
                    }
                    StoreOrdersLoadState.Ready -> {
                        if (filtered.isEmpty()) {
                            item {
                                EmptyState(icon = Icons.Default.Inventory, title = "No orders found")
                            }
                        } else {
                            items(filtered, key = { "${it.orderId}_${it.suborderFirestoreId}" }) { row ->
                                StoreOrderListCard(row = row, onClick = { onOpenOrder(row.orderId) })
                            }
                        }
                    }
                }
            }
        } else {
            // Messages Tab
            val currentUserId = viewModel.getCurrentUserId()
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (chats.isEmpty()) {
                    item {
                        EmptyState(icon = Icons.AutoMirrored.Filled.Chat, title = "No messages yet")
                    }
                } else {
                    items(chats, key = { it.id }) { chat ->
                        StoreChatCard(chat = chat, currentUserId = currentUserId, onClick = { onOpenChat(chat.storeId, chat.suborderId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
    }
}

@Composable
private fun StoreChatCard(chat: ChatThread, currentUserId: String, onClick: () -> Unit) {
    val isUnread = remember(chat, currentUserId) {
        val lastRead = chat.lastReadBy[currentUserId]
        lastRead == null || lastRead.seconds < chat.lastMessageTimestamp.seconds
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (isUnread) Color(0xFFF3F4FF) else Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        border = if (isUnread) BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.1f)) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(if (isUnread) Color(0xFF4338CA).copy(alpha = 0.1f) else Color(0xFFEEF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF4338CA))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(chat.customerName, fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium)
                    if (isUnread) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(8.dp).background(Color(0xFF4338CA), CircleShape))
                    }
                }
                Text(
                    "Order #${chat.suborderId.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    chat.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUnread) Color.Black else Color.Gray
                )
            }
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(chat.lastMessageTimestamp.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
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
        OrderStatus.ORDER_RECEIVED -> Quad("Received", Color(0xFFF3F4F6), Color(0xFF374151), Icons.Default.Schedule)
        OrderStatus.ORDER_CONFIRMED -> Quad("Confirmed", Color(0xFFE0E7FF), Color(0xFF4338CA), Icons.Default.Inventory)
        OrderStatus.PREPARING -> Quad("Preparing", Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Inventory)
        OrderStatus.SHIPPED -> Quad("Shipped", Color(0xFFDBEAFE), Color(0xFF1D4ED8), Icons.Default.LocalShipping)
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
        color = if (selected) Color(0xFF4338CA) else Color.White,
        shape = RoundedCornerShape(999.dp),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color(0xFF4B5563),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

private data class Quad(
    val label: String,
    val bg: Color,
    val fg: Color,
    val icon: ImageVector,
)
