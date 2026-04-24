package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ChatThread
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.viewmodel.OrderHistoryViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    viewModel: OrderHistoryViewModel,
    onBack: () -> Unit,
    onOpenOrderDetail: (orderId: String) -> Unit,
    onOpenChat: (storeId: String, suborderId: String) -> Unit,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
) {
    val orders by viewModel.orders.collectAsState()
    val chats by viewModel.chats.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Orders, 1: Messages

    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) orders
        else orders.filter { it.orderId.contains(searchQuery, ignoreCase = true) }
    }

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
    ) {
        // Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFF1F2937), Color(0xFF111827))
                    )
                )
                .padding(top = topPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "My Orders",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Order ID...", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    ),
                    singleLine = true
                )

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
                        text = { Text("Orders", fontWeight = FontWeight.SemiBold) }
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
                                    Badge(containerColor = Color.Red, contentColor = Color.White) {
                                        Text("$unreadCount")
                                    }
                                }
                            }) {
                                Text("Messages", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            if (orders.isEmpty()) {
                EmptyState(icon = Icons.Default.History, title = "No Orders Yet", subtitle = "Items you purchase will appear here for you to track and manage.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        OrderHistoryCard(
                            order = order,
                            onClick = { onOpenOrderDetail(order.orderId) },
                        )
                    }
                }
            }
        } else {
            if (chats.isEmpty()) {
                EmptyState(icon = Icons.Default.Chat, title = "No Messages", subtitle = "Conversation with stores about your orders will appear here.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    items(chats, key = { it.id }) { chat ->
                        CustomerChatCard(chat = chat, onClick = { onOpenChat(chat.storeId, chat.suborderId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF9CA3AF)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun CustomerChatCard(chat: ChatThread, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFEEF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF4338CA))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(chat.storeName, fontWeight = FontWeight.Bold)
                Text(
                    "Order #${chat.suborderId.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    chat.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatChatTime(chat.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private fun formatChatTime(timestamp: Timestamp): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
}

@Composable
private fun OrderHistoryCard(order: OrderDoc, onClick: () -> Unit) {
    val label = orderStatusLabelEnglish(order.status)
    val badgeColors = statusBadgeColors(order.status)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatOrderDate(order.createdAtMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        "Order #${order.orderId.takeLast(8).uppercase(Locale.US)}",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    color = badgeColors.first,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        label,
                        color = badgeColors.second,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                Text(
                    "$" + String.format(Locale.US, "%.2f", order.totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                )
            }
        }
    }
}

private fun formatOrderDate(ms: Long): String {
    if (ms <= 0L) return "—"
    return SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US).format(Date(ms))
}

private fun statusBadgeColors(status: String): Pair<Color, Color> = when (status) {
    "completed", "delivered" -> Color(0xFFDCFCE7) to Color(0xFF166534)
    "shipped" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
    "cancelled" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
    "preparing", "processing" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    "order_confirmed", "confirmed" -> Color(0xFFE0E7FF) to Color(0xFF4338CA)
    "order_received", "pending" -> Color(0xFFF3F4F6) to Color(0xFF374151)
    else -> Color(0xFFF3F4F6) to Color(0xFF374151)
}
