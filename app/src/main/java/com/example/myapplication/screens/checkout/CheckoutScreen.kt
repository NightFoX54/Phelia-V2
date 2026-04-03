package com.example.myapplication.screens.checkout

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.AppTopBar
import com.example.myapplication.models.Address
import com.example.myapplication.models.PaymentMethod

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (selectedAddress, setSelectedAddress) = remember { mutableIntStateOf(0) }
    val (selectedPayment, setSelectedPayment) = remember { mutableIntStateOf(0) }

    val items = listOf(
        Triple("Wireless Headphones Pro", 299.99, 1),
        Triple("Smartphone X12 Pro", 999.99, 2),
    )
    val addresses = listOf(
        Address("1", "Home", "123 Main Street", "New York, NY 10001", "+1 (555) 123-4567"),
        Address("2", "Office", "456 Business Ave", "New York, NY 10002", "+1 (555) 987-6543"),
    )
    val payments = listOf(
        PaymentMethod("1", "Credit Card", "Visa ending in 4242", "💳"),
        PaymentMethod("2", "Credit Card", "Mastercard ending in 8888", "💳"),
        PaymentMethod("3", "Digital Wallet", "Apple Pay", "📱"),
    )

    val subtotal = items.sumOf { it.second * it.third }
    val shipping = 15.0
    val tax = subtotal * 0.08
    val total = subtotal + shipping + tax

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    AppTopBar(title = "Checkout", onBack = onBack, containerColor = Color.White)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Shipping Address",
                ) {
                    addresses.forEachIndexed { index, address ->
                        SelectCard(
                            selected = selectedAddress == index,
                            onClick = { setSelectedAddress(index) },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(address.name, fontWeight = FontWeight.SemiBold)
                                        if (selectedAddress == index) {
                                            Spacer(modifier = Modifier.size(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center,
                                            ) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                        }
                                    }
                                    Text(address.addressLine, color = Color(0xFF4B5563))
                                    Text(address.cityLine, color = Color(0xFF4B5563))
                                    Text(address.phone, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    DashedButton("+  Add New Address")
                }

                SectionCard(
                    icon = Icons.Default.CreditCard,
                    title = "Payment Method",
                ) {
                    payments.forEachIndexed { index, pm ->
                        SelectCard(
                            selected = selectedPayment == index,
                            onClick = { setSelectedPayment(index) },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(pm.emoji, modifier = Modifier.padding(end = 10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pm.type, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                    Text(pm.name, fontWeight = FontWeight.SemiBold)
                                }
                                if (selectedPayment == index) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    DashedButton("+  Add New Card")
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Order Summary", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        items.forEach { (name, price, qty) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.SemiBold)
                                    Text("Qty: $qty", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                }
                                Text("$" + String.format("%.2f", price * qty), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        PriceRow("Subtotal", subtotal)
                        PriceRow("Shipping", shipping)
                        PriceRow("Tax (8%)", tax)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "By placing your order, you agree to our Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(horizontal = 10.dp),
                )

                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        Surface(
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Confirm Order - $" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SelectCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFEEF2FF) else Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB)),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) { content() }
    }
}

@Composable
private fun DashedButton(text: String) {
    Surface(
        onClick = {},
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD1D5DB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text(text, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = Color(0xFF4B5563), modifier = Modifier.weight(1f))
        Text("$" + String.format("%.2f", value), color = Color(0xFF4B5563))
    }
}

