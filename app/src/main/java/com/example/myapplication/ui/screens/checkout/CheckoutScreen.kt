package com.example.myapplication.ui.screens.checkout

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.PaymentMethodDoc
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.CheckoutViewModel
import com.example.myapplication.viewmodel.CustomerAccountViewModel
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    accountViewModel: CustomerAccountViewModel,
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit,
    onAddShippingAddress: () -> Unit,
    onAddPaymentMethod: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cartState by cartViewModel.uiState.collectAsState()
    val addresses by accountViewModel.shippingAddresses.collectAsState()
    val payments by accountViewModel.paymentMethods.collectAsState()
    val placing by checkoutViewModel.placing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedAddressId by remember { mutableStateOf<String?>(null) }
    var selectedPaymentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(addresses) {
        if (addresses.isEmpty()) {
            selectedAddressId = null
        } else if (selectedAddressId == null || addresses.none { it.addressId == selectedAddressId }) {
            selectedAddressId = addresses.firstOrNull { it.isDefault }?.addressId ?: addresses.first().addressId
        }
    }

    LaunchedEffect(payments) {
        if (payments.isEmpty()) {
            selectedPaymentId = null
        } else if (selectedPaymentId == null || payments.none { it.paymentMethodId == selectedPaymentId }) {
            selectedPaymentId = payments.firstOrNull { it.isDefault }?.paymentMethodId ?: payments.first().paymentMethodId
        }
    }

    val lines = cartState.lines
    val subtotal = lines.sumOf { it.unitPrice * it.quantity }
    val shipping = if (subtotal > 0) 15.0 else 0.0
    val tax = lines.sumOf { (it.unitPrice * it.quantity) * (it.taxRatePercent.coerceAtLeast(0) / 100.0) }
    val total = subtotal + shipping + tax

    val canPlaceOrder = lines.isNotEmpty() &&
        !cartState.isEnriching &&
        selectedAddressId != null &&
        selectedPaymentId != null &&
        !placing

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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Shipping Address",
                ) {
                    if (addresses.isEmpty()) {
                        Text(
                            "No shipping address on file. Add one to continue.",
                            color = Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        addresses.forEach { address ->
                            val selected = address.addressId == selectedAddressId
                            SelectCard(
                                selected = selected,
                                onClick = { selectedAddressId = address.addressId },
                            ) {
                                CheckoutAddressRow(address = address, selected = selected)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    DashedButton("+  Add New Address", onClick = onAddShippingAddress)
                }

                SectionCard(
                    icon = Icons.Default.CreditCard,
                    title = "Payment Method",
                ) {
                    if (payments.isEmpty()) {
                        Text(
                            "No payment method on file. Add a card to continue.",
                            color = Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        payments.forEach { pm ->
                            val selected = pm.paymentMethodId == selectedPaymentId
                            SelectCard(
                                selected = selected,
                                onClick = { selectedPaymentId = pm.paymentMethodId },
                            ) {
                                CheckoutPaymentRow(method = pm, selected = selected)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    DashedButton("+  Add payment method", onClick = onAddPaymentMethod)
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Order Summary", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (lines.isEmpty()) {
                            Text("Your cart is empty.", color = Color(0xFF6B7280))
                        } else {
                            lines.forEach { line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            line.productName,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            "Qty: ${line.quantity}",
                                            color = Color(0xFF6B7280),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Text(
                                        "$" + String.format("%.2f", line.unitPrice * line.quantity),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                        if (cartState.isEnriching && lines.isNotEmpty()) {
                            Text("Updating cart…", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        PriceRow("Subtotal", subtotal)
                        PriceRow("Shipping", shipping)
                        PriceRow("Tax", tax)
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
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                SnackbarHost(hostState = snackbarHostState)
                if (!canPlaceOrder && lines.isNotEmpty() && !placing) {
                    Text(
                        text = when {
                            addresses.isEmpty() -> "Add a shipping address to continue."
                            payments.isEmpty() -> "Add a payment method to continue."
                            cartState.isEnriching -> "Please wait…"
                            else -> ""
                        },
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = {
                        val address = addresses.find { it.addressId == selectedAddressId } ?: return@Button
                        val paymentId = selectedPaymentId ?: return@Button
                        checkoutViewModel.placeOrder(
                            lines = lines,
                            shippingAddress = address,
                            paymentMethodId = paymentId,
                            shippingFee = shipping,
                        ) { result ->
                            result.fold(
                                onSuccess = { orderId -> onOrderPlaced(orderId) },
                                onFailure = { e ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(e.message ?: "Could not place order")
                                    }
                                },
                            )
                        }
                    },
                    enabled = lines.isNotEmpty() &&
                        !cartState.isEnriching &&
                        selectedAddressId != null &&
                        selectedPaymentId != null &&
                        !placing,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    if (placing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Confirm Order - $" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutAddressRow(address: ShippingAddressDoc, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(address.label, fontWeight = FontWeight.SemiBold)
                if (selected) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Text(address.fullName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B5563))
            Text(address.line1, color = Color(0xFF4B5563), maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (address.line2.isNotBlank()) {
                Text(address.line2, color = Color(0xFF4B5563), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                listOf(address.district, address.city, address.postalCode, address.country).filter { it.isNotBlank() }.joinToString(", "),
                color = Color(0xFF4B5563),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(address.phone, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CheckoutPaymentRow(method: PaymentMethodDoc, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("💳", modifier = Modifier.padding(end = 10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(method.type.ifBlank { "Card" }, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            Text(
                if (method.maskedPan.isNotBlank()) method.maskedPan
                else "${method.brand.ifBlank { "Card" }} •••• ${method.last4}",
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(method.holderName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
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
private fun DashedButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
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
