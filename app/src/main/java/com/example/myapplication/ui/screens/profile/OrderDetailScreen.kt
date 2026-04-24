package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.model.OrderItemDoc
import com.example.myapplication.data.model.OrderStatus
import com.example.myapplication.data.model.SuborderDetailUi
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.OrderDetailUiState
import com.example.myapplication.viewmodel.OrderDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onOpenProduct: (productId: String) -> Unit,
    onMessageStore: (storeId: String, suborderId: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = viewModel(
        key = orderId,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OrderDetailViewModel(orderId = orderId) as T
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsState()
    var reviewTarget by remember { mutableStateOf<ReviewTarget?>(null) }
    var reviewSubmitting by remember { mutableStateOf(false) }

    val headerColor = if (state is OrderDetailUiState.Ready) {
        statusMainColor((state as OrderDetailUiState.Ready).data.order.status)
    } else {
        Color.White
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = headerColor, shadowElevation = if (headerColor == Color.White) 1.dp else 0.dp) {
            AppTopBar(
                title = "Order details",
                onBack = onBack,
                containerColor = headerColor,
            )
        }
        when (val s = state) {
            is OrderDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrderDetailUiState.Failed -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.message, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
            is OrderDetailUiState.Ready -> {
                OrderDetailContent(
                    data = s.data,
                    onOpenProduct = onOpenProduct,
                    onMessageStore = onMessageStore,
                    onRequestWriteReview = { target -> reviewTarget = target },
                )
            }
        }
    }

    val target = reviewTarget
    if (target != null) {
        WriteReviewDialog(
            productLabel = target.productName,
            submitting = reviewSubmitting,
            onDismiss = { if (!reviewSubmitting) reviewTarget = null },
            onSubmit = { rating, comment ->
                reviewSubmitting = true
                viewModel.submitProductReview(
                    suborderId = target.suborderId,
                    itemId = target.itemId,
                    productId = target.productId,
                    rating = rating,
                    comment = comment,
                ) { result ->
                    reviewSubmitting = false
                    result.onSuccess { reviewTarget = null }
                }
            },
        )
    }
}

private data class ReviewTarget(
    val suborderId: String,
    val itemId: String,
    val productId: String,
    val productName: String,
)

@Composable
private fun OrderDetailContent(
    data: OrderDetailBundle,
    onOpenProduct: (productId: String) -> Unit,
    onMessageStore: (storeId: String, suborderId: String) -> Unit,
    onRequestWriteReview: (ReviewTarget) -> Unit,
) {
    val order = data.order
    val label = orderStatusLabelEnglish(order.status)
    val badgeColors = statusBadgeColors(order.status)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                formatOrderDetailDate(order.createdAtMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                            )
                            Text(
                                "Order #${order.orderId}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Surface(color = badgeColors.first, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                label,
                                color = badgeColors.second,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                    PriceLine("Subtotal (items)", order.totalPrice - order.totalTax - order.shippingFee)
                    PriceLine("Tax", order.totalTax)
                    PriceLine("Shipping", order.shippingFee)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(
                            "$" + String.format(Locale.US, "%.2f", order.totalPrice),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        "Payment ref: ${order.paymentMethodId.takeLast(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                }
            }

        Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Shipping address", fontWeight = FontWeight.Bold)
                    if (data.shippingAddressLines.isEmpty()) {
                        Text("—", color = Color(0xFF6B7280))
                    } else {
                        data.shippingAddressLines.forEach { line ->
                            Text(line, color = Color(0xFF374151), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

        data.suborders.forEach { sub ->
            SuborderSection(
                parentOrderStatus = order.status,
                sub = sub,
                onOpenProduct = onOpenProduct,
                onMessageStore = onMessageStore,
                onRequestWriteReview = onRequestWriteReview,
            )
        }
    }
}

@Composable
private fun SuborderSection(
    parentOrderStatus: String,
    sub: SuborderDetailUi,
    onOpenProduct: (productId: String) -> Unit,
    onMessageStore: (storeId: String, suborderId: String) -> Unit,
    onRequestWriteReview: (ReviewTarget) -> Unit,
) {
    val so = sub.suborder
    val subLabel = orderStatusLabelEnglish(so.status)
    val badgeColors = statusBadgeColors(so.status)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(sub.storeName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Store ID: ${so.storeId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onMessageStore(so.storeId, so.suborderId) }) {
                        Text("Message", style = MaterialTheme.typography.labelMedium)
                    }
                    Surface(color = badgeColors.first, shape = RoundedCornerShape(999.dp)) {
                        Text(
                            subLabel,
                            color = badgeColors.second,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            sub.items.forEach { item ->
                OrderItemRow(
                    parentOrderStatus = parentOrderStatus,
                    suborderId = so.suborderId,
                    item = item,
                    onOpenProduct = onOpenProduct,
                    onRequestWriteReview = onRequestWriteReview,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Merchandise", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                Text("$" + String.format(Locale.US, "%.2f", so.totalPrice))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Tax", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                Text("$" + String.format(Locale.US, "%.2f", so.totalTax))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Store total", fontWeight = FontWeight.SemiBold)
                Text(
                    "$" + String.format(Locale.US, "%.2f", so.totalPrice + so.totalTax),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun OrderItemRow(
    parentOrderStatus: String,
    suborderId: String,
    item: OrderItemDoc,
    onOpenProduct: (productId: String) -> Unit,
    onRequestWriteReview: (ReviewTarget) -> Unit,
) {
    val lineMerch = item.unitPrice * item.quantity
    val canOpen = item.productId.isNotBlank()
    val canWriteReview = orderAllowsProductReview(parentOrderStatus) && item.review == null && canOpen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canOpen) { onOpenProduct(item.productId) },
        ) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            if (item.variant.isNotEmpty()) {
                Text(
                    item.variant.entries.joinToString(" · ") { "${it.key}: ${it.value}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${String.format(Locale.US, "%.2f", item.unitPrice)} × ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4B5563),
                )
                Text(
                    "$" + String.format(Locale.US, "%.2f", lineMerch),
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                "Tax: $" + String.format(Locale.US, "%.2f", item.tax),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
            )
        }
        item.review?.let { r ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your review", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
            val n = r.rating.roundToInt().coerceIn(0, 5)
            Text(
                "★".repeat(n) + "☆".repeat(5 - n),
                color = Color(0xFFF59E0B),
                fontSize = 14.sp,
            )
            Text(r.comment, style = MaterialTheme.typography.bodySmall, color = Color(0xFF374151))
        }
        if (canWriteReview) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    onRequestWriteReview(
                        ReviewTarget(
                            suborderId = suborderId,
                            itemId = item.itemId,
                            productId = item.productId,
                            productName = item.name,
                        ),
                    )
                },
            ) {
                Text("Write review")
            }
        }
    }
}

@Composable
private fun WriteReviewDialog(
    productLabel: String,
    submitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit,
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Product review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(productLabel, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { index ->
                        val filled = index < rating
                        Text(
                            if (filled) "★" else "☆",
                            fontSize = 28.sp,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.clickable(enabled = !submitting) {
                                rating = index + 1
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    enabled = !submitting,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating.toDouble(), comment) },
                enabled = !submitting && comment.isNotBlank(),
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text("Cancel")
            }
        },
    )
}

private fun orderAllowsProductReview(orderStatus: String): Boolean =
    orderStatus == OrderStatus.COMPLETED || orderStatus == "delivered"

@Composable
private fun PriceLine(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
        Text("$" + String.format(Locale.US, "%.2f", value), color = Color(0xFF374151))
    }
}

private fun formatOrderDetailDate(ms: Long): String {
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

private fun statusMainColor(status: String): Color = when (status) {
    OrderStatus.COMPLETED, "delivered" -> Color(0xFF10B981) // Green
    OrderStatus.SHIPPED -> Color(0xFF3B82F6) // Blue
    OrderStatus.CANCELLED -> Color(0xFFEF4444) // Red
    OrderStatus.PREPARING, "processing" -> Color(0xFFF59E0B) // Orange
    OrderStatus.ORDER_CONFIRMED, "confirmed" -> Color(0xFF6366F1) // Indigo
    OrderStatus.ORDER_RECEIVED, "pending" -> Color(0xFF6B7280) // Gray
    OrderStatus.IN_PROGRESS -> Color(0xFF0D9488) // Teal
    else -> Color(0xFF6B7280)
}
