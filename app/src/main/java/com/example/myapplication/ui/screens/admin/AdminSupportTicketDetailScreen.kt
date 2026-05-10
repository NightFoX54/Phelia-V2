package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.orderPublicLabel
import com.example.myapplication.data.repository.NotificationTypes
import com.example.myapplication.viewmodel.AdminSupportTicketDetailUiState
import com.example.myapplication.viewmodel.AdminSupportTicketDetailViewModel
import com.example.myapplication.viewmodel.AdminSupportTicketDetailViewModelFactory
import com.example.myapplication.viewmodel.UserSettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminSupportTicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
    userSettingsViewModel: UserSettingsViewModel,
    viewModel: AdminSupportTicketDetailViewModel = viewModel(
        factory = AdminSupportTicketDetailViewModelFactory(ticketId),
    ),
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            AppTopBar(title = "Ticket", onBack = onBack)
        }
        when (val s = state) {
            AdminSupportTicketDetailUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(48.dp))
            }
            is AdminSupportTicketDetailUiState.Failed -> {
                Text(s.message, color = Color(0xFFDC2626), modifier = Modifier.padding(20.dp))
            }
            is AdminSupportTicketDetailUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TicketCard(s.ticket.customerName, s.ticket.customerEmail, s.ticket.customerMessage, s.ticket.orderReferenceRaw)
                    OrderResolutionCard(s.ticket.resolvedOrderId, s.order)
                    Button(
                        onClick = {
                            scope.launch {
                                userSettingsViewModel.syncDismissNotificationsMatching(
                                    type = NotificationTypes.SUPPORT_TICKET_SUBMITTED,
                                    orderId = ticketId,
                                )
                                viewModel.markClosed()
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Mark resolved")
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(name: String, email: String, message: String, rawRef: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Customer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(name.ifBlank { "—" }, fontWeight = FontWeight.SemiBold)
            Text(email, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text("Reference pasted", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text(rawRef.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text("Message", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OrderResolutionCard(resolvedOrderId: String, bundle: OrderDetailBundle?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Order details", fontWeight = FontWeight.Bold)
            if (resolvedOrderId.isBlank()) {
                Text(
                    "Could not match this reference to an order for this customer.",
                    color = Color(0xFF92400E),
                    style = MaterialTheme.typography.bodySmall,
                )
                return@Column
            }
            if (bundle == null) {
                Text("Order id: $resolvedOrderId — failed to load details.", color = Color(0xFFDC2626))
                return@Column
            }
            val o = bundle.order
            Text("${orderPublicLabel(o.orderId)} · ${o.orderId}", fontWeight = FontWeight.SemiBold)
            Text("Status: ${orderStatusLabelEnglish(o.status)}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Placed: ${formatDate(o.createdAtMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
            )
            Text(
                "Total: $" + String.format(Locale.US, "%.2f", o.totalPrice),
                fontWeight = FontWeight.Medium,
            )
            if (bundle.shippingAddressLines.isNotEmpty()) {
                HorizontalDivider()
                Text("Shipping", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                bundle.shippingAddressLines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
            bundle.suborders.forEach { sub ->
                HorizontalDivider()
                Text(sub.storeName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text(
                    "Package: ${orderStatusLabelEnglish(sub.suborder.status)} · $" +
                        String.format(Locale.US, "%.2f", sub.suborder.totalPrice + sub.suborder.totalTax),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
                sub.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${item.name} × ${item.quantity}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "$" + String.format(Locale.US, "%.2f", item.unitPrice * item.quantity),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(ms: Long): String {
    if (ms <= 0L) return "—"
    return SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US).format(Date(ms))
}
