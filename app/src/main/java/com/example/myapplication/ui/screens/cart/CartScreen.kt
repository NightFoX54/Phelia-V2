package com.example.myapplication.ui.screens.cart

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.CartViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cartUi by cartViewModel.uiState.collectAsState()
    val stockMessages by cartViewModel.stockMessages.collectAsState()

    LaunchedEffect(Unit) {
        cartViewModel.validateStockOnOpen()
    }

    val lines = cartUi.lines
    val subtotal = lines.sumOf { it.unitPrice * it.quantity }
    val shipping = 15.0
    val total = subtotal + shipping

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppTopBar(title = "My Cart", onBack = onBack, containerColor = Color.White)
                Text(
                    text = "${lines.size} " + if (lines.size == 1) "item" else "items",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        if (stockMessages.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        stockMessages.forEach { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9A3412),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                    IconButton(onClick = { cartViewModel.clearStockMessages() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF9A3412))
                    }
                }
            }
        }

        if (cartUi.isEnriching && lines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = Color(0xFF6B7280))
            }
        } else if (lines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🛒", fontSize = 40.sp)
                    }
                    Text("Your cart is empty", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    Text("Browse the store to add items", color = Color(0xFF6B7280), modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                lines.forEach { item ->
                    val atMax = item.quantity >= item.maxStock
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(
                                model = item.imageUrl.takeIf { it.isNotBlank() },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF3F4F6)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        item.brandName?.let { b ->
                                            Text(b, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                                        }
                                        Text(item.productName, fontWeight = FontWeight.SemiBold)
                                    }
                                    IconButton(
                                        onClick = {
                                            cartViewModel.remove(item.productId, item.variantId)
                                        },
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp),
                                ) {
                                    item.attributes.forEach { (k, v) ->
                                        VariantPill("$k: $v")
                                    }
                                }
                                Text(
                                    "$" + String.format("%.2f", item.unitPrice),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(top = 10.dp),
                                ) {
                                    QtyButton(onClick = {
                                        cartViewModel.changeQuantity(item.productId, item.variantId, -1)
                                    }) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = Color(0xFF374151))
                                    }
                                    Text(item.quantity.toString(), fontWeight = FontWeight.SemiBold)
                                    QtyButton(
                                        primary = true,
                                        onClick = {
                                            if (!atMax) {
                                                cartViewModel.changeQuantity(item.productId, item.variantId, 1)
                                            }
                                        },
                                        modifier = Modifier.alpha(if (atMax) 0.4f else 1f),
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Order summary", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("Subtotal", subtotal)
                        SummaryRow("Shipping", shipping)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(86.dp))
            }

            Surface(
                shadowElevation = 10.dp,
                color = Color.White,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Button(
                        onClick = onCheckout,
                        enabled = lines.isNotEmpty(),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text("Checkout — $" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B5563))
    }
}

@Composable
private fun QtyButton(
    primary: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (primary) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6),
        shape = RoundedCornerShape(999.dp),
        modifier = modifier.size(34.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, color = Color(0xFF4B5563), modifier = Modifier.weight(1f))
        Text("$" + String.format("%.2f", value), color = Color(0xFF4B5563))
    }
}
