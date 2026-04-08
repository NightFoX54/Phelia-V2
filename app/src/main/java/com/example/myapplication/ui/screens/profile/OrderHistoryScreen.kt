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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.OrderHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    viewModel: OrderHistoryViewModel,
    onBack: () -> Unit,
    onOpenOrderDetail: (orderId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            AppTopBar(title = "Order History", onBack = onBack, containerColor = Color.White)
        }
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "You have no orders yet.",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            ) {
                items(orders, key = { it.orderId }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onClick = { onOpenOrderDetail(order.orderId) },
                    )
                }
            }
        }
    }
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
