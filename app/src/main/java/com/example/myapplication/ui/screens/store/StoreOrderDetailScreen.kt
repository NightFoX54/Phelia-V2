package com.example.myapplication.ui.screens.store

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreOrderDetailBundle
import com.example.myapplication.data.model.StoreOrderItemLine
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.StoreOrderDetailUiState
import com.example.myapplication.viewmodel.StoreOrderDetailViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun StoreOrderDetailScreen(
    viewModel: StoreOrderDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val updating by viewModel.updating.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        delay(3500)
        viewModel.clearMessage()
    }

    Column(
        modifier = modifier.background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            AppTopBar(
                title = when (val s = uiState) {
                    is StoreOrderDetailUiState.Ready -> "Order #${s.bundle.order.orderId.takeLast(8).uppercase(Locale.US)}"
                    else -> "Order"
                },
                onBack = onBack,
                containerColor = Color.White,
            )
        }

        if (message != null) {
            Text(
                message!!,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        when (val state = uiState) {
            StoreOrderDetailUiState.Loading -> {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is StoreOrderDetailUiState.Error -> {
                Text(state.message, color = Color(0xFFDC2626), modifier = Modifier.padding(20.dp))
                TextButton(onClick = { viewModel.load() }, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Retry")
                }
            }
            is StoreOrderDetailUiState.Ready -> {
                StoreOrderDetailBody(
                    bundle = state.bundle,
                    allowedNext = state.allowedNextStatuses,
                    updating = updating,
                    onStatusSelected = { viewModel.updateSuborderStatus(it) },
                )
            }
        }
    }
}

@Composable
private fun StoreOrderDetailBody(
    bundle: StoreOrderDetailBundle,
    allowedNext: List<String>,
    updating: Boolean,
    onStatusSelected: (String) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val pkg = bundle.ourSuborder
    val horizontalCardPadding = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Your package status", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(orderStatusLabelEnglish(pkg.status), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (allowedNext.isNotEmpty()) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            TextButton(onClick = { menuOpen = true }, enabled = !updating) {
                                Text("Update status")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                allowedNext.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(orderStatusLabelEnglish(st)) },
                                        onClick = {
                                            menuOpen = false
                                            onStatusSelected(st)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                if (updating) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Updating…", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = horizontalCardPadding,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Whole order (customer)", fontWeight = FontWeight.Bold)
                    Text(
                        orderStatusLabelEnglish(bundle.order.status),
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (bundle.order.status == com.example.myapplication.data.model.OrderStatus.IN_PROGRESS) {
                        Text(
                            "Other stores in this order may still be processing their packages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }

        item {
            InfoCardStore(
                title = "Customer",
                modifier = horizontalCardPadding,
                rows = listOf(
                    Triple(Icons.Default.Person, "Name", bundle.buyerName),
                    Triple(Icons.Default.Mail, "Email", bundle.buyerEmail.ifBlank { "—" }),
                ),
            )
        }

        item {
            InfoCardStore(
                title = "Shipping address",
                modifier = horizontalCardPadding,
                rows = listOf(Triple(Icons.Default.LocationOn, "Address", bundle.shippingAddressLines.joinToString("\n").ifBlank { "—" })),
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = horizontalCardPadding,
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Items", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    bundle.items.forEach { line ->
                        OrderLineRow(line = line)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = horizontalCardPadding,
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your store totals", fontWeight = FontWeight.Bold)
                    SummaryRowStore("Merchandise", pkg.totalPrice)
                    SummaryRowStore("Tax", pkg.totalTax)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                    val total = pkg.totalPrice + pkg.totalTax
                    SummaryRowStore("Total", total, bold = true)
                }
            }
        }

        item {
            Text(
                "Customer order total (all stores): $" + String.format(Locale.US, "%.2f", bundle.order.totalPrice),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun OrderLineRow(line: StoreOrderItemLine) {
    val item = line.item
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        if (!line.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = line.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            val v = item.variant.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            if (v.isNotBlank()) {
                Text(v, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                Text(
                    "Qty ${item.quantity}",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$" + String.format(Locale.US, "%.2f", item.unitPrice * item.quantity),
                    color = Color(0xFF4338CA),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InfoCardStore(
    title: String,
    modifier: Modifier = Modifier,
    rows: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String>>,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            rows.forEach { (icon, label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                        Text(value)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRowStore(label: String, value: Double, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("$" + String.format(Locale.US, "%.2f", value), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
