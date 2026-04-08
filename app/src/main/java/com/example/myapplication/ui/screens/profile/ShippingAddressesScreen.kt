package com.example.myapplication.ui.screens.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.repository.ShippingAddressInput
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.CustomerAccountViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingAddressesScreen(
    accountViewModel: CustomerAccountViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val addresses by accountViewModel.shippingAddresses.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var editing: ShippingAddressDoc? by remember { mutableStateOf(null) }
    var deleteTarget: ShippingAddressDoc? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                AppTopBar(title = "Shipping Address", onBack = onBack, containerColor = Color.White)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding),
        ) {
            if (addresses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No saved addresses yet.", color = Color(0xFF6B7280))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    items(addresses, key = { it.addressId }) { addr ->
                        AddressCard(
                            address = addr,
                            onEdit = {
                                editing = addr
                                showForm = true
                            },
                            onSetDefault = {
                                accountViewModel.setDefaultShippingAddress(addr.addressId) { r ->
                                    scope.launch {
                                        if (r.isFailure) {
                                            snackbarHostState.showSnackbar(r.exceptionOrNull()?.message ?: "Could not update")
                                        }
                                    }
                                }
                            },
                            onDelete = { deleteTarget = addr },
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
                Text("Add New Address")
            }
        }
    }

    if (showForm) {
        AddressFormDialog(
            initial = editing,
            onDismiss = {
                showForm = false
                editing = null
            },
            onSave = { input, existingId ->
                accountViewModel.saveShippingAddress(existingId, input) { r ->
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
            title = { Text("Delete address?") },
            text = { Text("“${target.label}” will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountViewModel.deleteShippingAddress(target.addressId) { r ->
                            scope.launch {
                                deleteTarget = null
                                if (r.isFailure) {
                                    snackbarHostState.showSnackbar(r.exceptionOrNull()?.message ?: "Delete failed")
                                }
                            }
                        }
                    },
                ) { Text("Delete", color = Color(0xFFDC2626)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AddressCard(
    address: ShippingAddressDoc,
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
                Text(address.label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (address.isDefault) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            "Default",
                            color = Color(0xFF4338CA),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            Text(address.fullName, fontWeight = FontWeight.Medium)
            val line1 = address.line1 + address.line2.takeIf { it.isNotBlank() }?.let { ", $it" }.orEmpty()
            Text(line1, color = Color(0xFF4B5563))
            Text(
                listOf(address.district, address.city, address.postalCode).filter { it.isNotBlank() }.joinToString(", "),
                color = Color(0xFF4B5563),
            )
            Text(address.country, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            Text(address.phone, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileChip("Edit", onClick = onEdit)
                if (!address.isDefault) ProfileChip("Set default", onClick = onSetDefault)
                ProfileChip("Delete", warning = true, onClick = onDelete)
            }
        }
    }
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

@Composable
private fun AddressFormDialog(
    initial: ShippingAddressDoc?,
    onDismiss: () -> Unit,
    onSave: (ShippingAddressInput, String?) -> Unit,
) {
    var label by remember { mutableStateOf(initial?.label.orEmpty()) }
    var fullName by remember { mutableStateOf(initial?.fullName.orEmpty()) }
    var phone by remember { mutableStateOf(initial?.phone.orEmpty()) }
    var line1 by remember { mutableStateOf(initial?.line1.orEmpty()) }
    var line2 by remember { mutableStateOf(initial?.line2.orEmpty()) }
    var district by remember { mutableStateOf(initial?.district.orEmpty()) }
    var city by remember { mutableStateOf(initial?.city.orEmpty()) }
    var postalCode by remember { mutableStateOf(initial?.postalCode.orEmpty()) }
    var country by remember { mutableStateOf(initial?.country.orEmpty().ifBlank { "TR" }) }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: true) }

    LaunchedEffect(initial?.addressId) {
        label = initial?.label.orEmpty()
        fullName = initial?.fullName.orEmpty()
        phone = initial?.phone.orEmpty()
        line1 = initial?.line1.orEmpty()
        line2 = initial?.line2.orEmpty()
        district = initial?.district.orEmpty()
        city = initial?.city.orEmpty()
        postalCode = initial?.postalCode.orEmpty()
        country = initial?.country.orEmpty().ifBlank { "TR" }
        isDefault = initial?.isDefault ?: true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add address" else "Edit address") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. Home)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = line1, onValueChange = { line1 = it }, label = { Text("Address line 1") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = line2, onValueChange = { line2 = it }, label = { Text("Address line 2 (optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("Postal code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default address")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (line1.isBlank() || city.isBlank() || fullName.isBlank()) return@TextButton
                    val input = ShippingAddressInput(
                        label = label.ifBlank { "Address" },
                        fullName = fullName,
                        phone = phone,
                        line1 = line1,
                        line2 = line2,
                        district = district,
                        city = city,
                        postalCode = postalCode,
                        country = country,
                        isDefault = isDefault,
                    )
                    onSave(input, initial?.addressId)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
