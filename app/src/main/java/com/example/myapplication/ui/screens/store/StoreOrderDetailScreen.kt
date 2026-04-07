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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AppTopBar

private data class OrderItem(
    val id: String,
    val name: String,
    val image: String,
    val variant: String,
    val quantity: Int,
    val price: Double,
)

private data class OrderDetailUi(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val status: String,
    val date: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val shipping: Double,
    val tax: Double,
    val total: Double,
    val address: String,
)

@Composable
fun StoreOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val order = orderDetail(orderId)
    if (order == null) {
        Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
            AppTopBar(title = "Order Not Found", onBack = onBack)
            Text("Order not found", modifier = Modifier.padding(20.dp))
        }
        return
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            AppTopBar(title = order.orderNumber, onBack = onBack, containerColor = Color.White)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(order.date, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
                    StatusBadge(order.status)
                }
            }
            item { InfoCard("Customer Information", listOf(
                Triple(Icons.Default.Person, "Name", order.customerName),
                Triple(Icons.Default.Mail, "Email", order.customerEmail),
                Triple(Icons.Default.Phone, "Phone", order.customerPhone),
            )) }
            item { InfoCard("Shipping Address", listOf(
                Triple(Icons.Default.LocationOn, "Address", order.address),
            )) }
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Order Items (${order.items.size})", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        order.items.forEach { item ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                                AsyncImage(model = item.image, contentDescription = null, modifier = Modifier.size(72.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    Text(item.variant, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                    Row {
                                        Text("Qty: ${item.quantity}", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        Text("$" + String.format("%.2f", item.price * item.quantity), color = Color(0xFF4338CA), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order Summary", fontWeight = FontWeight.Bold)
                        SummaryRow("Subtotal", order.subtotal)
                        SummaryRow("Shipping", order.shipping)
                        SummaryRow("Tax", order.tax)
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                        SummaryRow("Total", order.total, bold = true)
                    }
                }
            }
            item {
                when (order.status) {
                    "pending" -> Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Accept Order") }
                        Button(onClick = {}, modifier = Modifier.weight(1f), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) { Text("Cancel Order") }
                    }
                    "processing" -> Button(onClick = {}, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text("Mark as Completed") }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, rows: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String>>) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            rows.forEach { (icon, label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                    Column {
                        Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                        Text(value)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg, label) = when (status) {
        "pending" -> Triple(Color(0xFFFEF9C3), Color(0xFFA16207), "Pending")
        "processing" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "Processing")
        "completed" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Completed")
        else -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Cancelled")
    }
    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Text(label, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("$" + String.format("%.2f", value), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun orderDetail(id: String): OrderDetailUi? {
    val map = mapOf(
        "1" to OrderDetailUi("1", "ORD-2024-001", "John Doe", "john.doe@email.com", "+1 (555) 123-4567", "processing", "Mar 20, 2024",
            listOf(OrderItem("1", "Wireless Headphones Pro", "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", "Black, 256GB", 2, 299.99)),
            599.98, 15.0, 85.0, 699.98, "123 Main Street, Apt 4B, New York, NY 10001"),
        "2" to OrderDetailUi("2", "ORD-2024-002", "Sarah Smith", "sarah.smith@email.com", "+1 (555) 234-5678", "completed", "Mar 19, 2024",
            listOf(OrderItem("2", "Laptop Pro 15", "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", "Space Gray, 512GB", 1, 1299.99)),
            1299.99, 0.0, 0.0, 1299.99, "456 Oak Avenue, Los Angeles, CA 90001"),
    )
    return map[id] ?: map["1"]
}

