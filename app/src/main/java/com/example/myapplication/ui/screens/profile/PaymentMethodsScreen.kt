package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.CardPanUtils
import com.example.myapplication.data.model.PaymentMethodDoc
import com.example.myapplication.data.repository.PaymentMethodInput
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.CustomerAccountViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    accountViewModel: CustomerAccountViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val methods by accountViewModel.paymentMethods.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var editing: PaymentMethodDoc? by remember { mutableStateOf(null) }
    var deleteTarget: PaymentMethodDoc? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                AppTopBar(title = "Payment Methods", onBack = onBack, containerColor = Color.White)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding),
        ) {
            if (methods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No saved payment methods yet.", color = Color(0xFF6B7280))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    items(methods, key = { it.paymentMethodId }) { pm ->
                        PaymentCard(
                            method = pm,
                            onEdit = {
                                editing = pm
                                showForm = true
                            },
                            onSetDefault = {
                                accountViewModel.setDefaultPaymentMethod(pm.paymentMethodId) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snackbarHostState.showSnackbar(r.exceptionOrNull()?.message ?: "Could not update")
                                        }
                                    }
                                }
                            },
                            onDelete = { deleteTarget = pm },
                        )
                    }
                }
            }
            Button(
                onClick = {
                    editing = null
                    showForm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Add payment method")
            }
        }
    }

    if (showForm) {
        PaymentFormDialog(
            initial = editing,
            onDismiss = {
                showForm = false
                editing = null
            },
            onSave = { input, existingId ->
                accountViewModel.savePaymentMethod(existingId, input) { r ->
                    scope.launch {
                        if (r.isSuccess) {
                            showForm = false
                            editing = null
                        } else {
                            snackbarHostState.showSnackbar(r.exceptionOrNull()?.message ?: "Save failed")
                        }
                    }
                }
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Remove payment method?") },
            text = { Text("“${target.label}” will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountViewModel.deletePaymentMethod(target.paymentMethodId) { r ->
                            scope.launch {
                                deleteTarget = null
                                if (r.isFailure) {
                                    snackbarHostState.showSnackbar(r.exceptionOrNull()?.message ?: "Remove failed")
                                }
                            }
                        }
                    },
                ) { Text("Remove", color = Color(0xFFDC2626)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PaymentCard(
    method: PaymentMethodDoc,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(method.label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (method.isDefault) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            "Primary",
                            color = Color(0xFF4338CA),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            Text(
                if (method.maskedPan.isNotBlank()) method.maskedPan
                else "${method.brand.ifBlank { "Card" }} •••• ${method.last4}",
                fontWeight = FontWeight.Medium,
            )
            Text(
                method.holderName,
                color = Color(0xFF4B5563),
            )
            Text(
                "Expires %02d/%d".format(method.expiryMonth, method.expiryYear % 100),
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileChip("Edit", onClick = onEdit)
                if (!method.isDefault) ProfileChip("Set primary", onClick = onSetDefault)
                ProfileChip("Remove", warning = true, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun PaymentFormDialog(
    initial: PaymentMethodDoc?,
    onDismiss: () -> Unit,
    onSave: (PaymentMethodInput, String?) -> Unit,
) {
    var label by remember { mutableStateOf(initial?.label.orEmpty()) }
    var brand by remember { mutableStateOf(initial?.brand.orEmpty()) }
    var cardDigits by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf(initial?.holderName.orEmpty()) }
    var monthStr by remember { mutableStateOf(initial?.expiryMonth?.toString()?.padStart(2, '0') ?: "01") }
    var yearStr by remember { mutableStateOf(initial?.expiryYear?.toString() ?: "2030") }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: true) }

    LaunchedEffect(initial?.paymentMethodId) {
        label = initial?.label.orEmpty()
        brand = initial?.brand.orEmpty()
        cardDigits = ""
        cvv = ""
        holderName = initial?.holderName.orEmpty()
        monthStr = initial?.expiryMonth?.toString()?.padStart(2, '0') ?: "01"
        yearStr = initial?.expiryYear?.toString() ?: "2030"
        isDefault = initial?.isDefault ?: true
    }

    val panDigits = CardPanUtils.digitsOnly(cardDigits)
    val cardDisplay = CardPanUtils.formatGrouped(panDigits)
    val isNew = initial == null
    val replacingCard = panDigits.length >= CardPanUtils.MIN_PAN_LENGTH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add payment method" else "Edit payment method") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Card number and CVV are not stored on the server; only a masked number and last digits are saved (like production apps with a payment provider).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
                initial?.let { doc ->
                    Text(
                        "Current: ${if (doc.maskedPan.isNotBlank()) doc.maskedPan else "•••• ${doc.last4}"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "Enter a new card number only if you want to replace this card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                }
                OutlinedTextField(
                    value = cardDisplay,
                    onValueChange = { cardDigits = CardPanUtils.digitsOnly(it) },
                    label = { Text("Card number") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { v -> if (v.length <= 4 && v.all { it.isDigit() }) cvv = v },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(value = holderName, onValueChange = { holderName = it }, label = { Text("Name on card") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { v ->
                            if (v.length <= 2 && v.all { it.isDigit() }) monthStr = v
                        },
                        label = { Text("MM") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { v ->
                            if (v.length <= 4 && v.all { it.isDigit() }) yearStr = v
                        },
                        label = { Text("YYYY") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (optional, auto from number)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as primary")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val m = monthStr.toIntOrNull()?.coerceIn(1, 12) ?: 1
                    val y = yearStr.toIntOrNull()?.coerceAtLeast(2024) ?: 2030
                    if (holderName.isBlank()) return@TextButton
                    val cvvOk = when {
                        isNew -> cvv.length in 3..4
                        replacingCard -> cvv.length in 3..4
                        else -> true
                    }
                    if (!cvvOk) return@TextButton
                    if (isNew) {
                        if (panDigits.length < CardPanUtils.MIN_PAN_LENGTH || !CardPanUtils.luhnCheck(panDigits)) return@TextButton
                    } else if (replacingCard) {
                        if (!CardPanUtils.luhnCheck(panDigits)) return@TextButton
                    }
                    val inferred = if (brand.isBlank() && panDigits.length >= CardPanUtils.MIN_PAN_LENGTH) {
                        CardPanUtils.inferBrand(panDigits)
                    } else {
                        brand
                    }
                    val input = PaymentMethodInput(
                        label = label.ifBlank {
                            if (panDigits.length >= CardPanUtils.MIN_PAN_LENGTH) {
                                "${inferred.ifBlank { "Card" }} •••• ${CardPanUtils.last4(panDigits)}"
                            } else {
                                initial?.label.orEmpty().ifBlank { "Card" }
                            }
                        },
                        type = "card",
                        brand = inferred.ifBlank { initial?.brand.orEmpty() },
                        holderName = holderName,
                        expiryMonth = m,
                        expiryYear = y,
                        isDefault = isDefault,
                        panDigits = panDigits,
                    )
                    onSave(input, initial?.paymentMethodId)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ProfileChip(text: String, warning: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (warning) Color(0xFFFEF2F2) else Color(0xFFF3F4F6),
    ) {
        Text(
            text = text,
            color = if (warning) Color(0xFFDC2626) else Color(0xFF374151),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
