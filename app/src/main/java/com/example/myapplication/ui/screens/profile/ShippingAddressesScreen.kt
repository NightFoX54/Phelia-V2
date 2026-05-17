package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.repository.ShippingAddressInput
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.util.ShippingAddressValidation
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
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                AppTopBar(title = "Shipping Address", onBack = onBack)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    Text("No saved addresses yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            "Default",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            Text(address.fullName, fontWeight = FontWeight.Medium)
            val line1 = address.line1 + address.line2.takeIf { it.isNotBlank() }?.let { ", $it" }.orEmpty()
            Text(line1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                listOf(address.district, address.city, address.postalCode).filter { it.isNotBlank() }.joinToString(", "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(address.country, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text(address.phone, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (warning) scheme.errorContainer else scheme.surfaceVariant,
    ) {
        Text(
            text = text,
            color = if (warning) scheme.onErrorContainer else scheme.onSurfaceVariant,
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

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
        errors = emptyMap()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text(if (initial == null) "Add address" else "Edit address") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it; errors = errors - "label" },
                    label = { Text("Label (e.g. Home) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("label"),
                    supportingText = {
                        errors["label"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errors = errors - "fullName" },
                    label = { Text("Full name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("fullName"),
                    supportingText = {
                        errors["fullName"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errors = errors - "phone" },
                    label = { Text("Phone *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("phone"),
                    supportingText = {
                        errors["phone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = line1,
                    onValueChange = { line1 = it; errors = errors - "line1" },
                    label = { Text("Address line 1 *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("line1"),
                    supportingText = {
                        errors["line1"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(value = line2, onValueChange = { line2 = it }, label = { Text("Address line 2 (optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it; errors = errors - "district" },
                    label = { Text("District *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("district"),
                    supportingText = {
                        errors["district"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it; errors = errors - "city" },
                    label = { Text("City *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("city"),
                    supportingText = {
                        errors["city"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it; errors = errors - "postalCode" },
                    label = { Text("Postal code *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("postalCode"),
                    supportingText = {
                        errors["postalCode"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it; errors = errors - "country" },
                    label = { Text("Country *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errors.containsKey("country"),
                    supportingText = {
                        errors["country"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default address")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newErrors = buildMap {
                        ShippingAddressValidation.validateLabel(label)?.let { put("label", it) }
                        ShippingAddressValidation.validateFullName(fullName)?.let { put("fullName", it) }
                        ShippingAddressValidation.validatePhone(phone)?.let { put("phone", it) }
                        ShippingAddressValidation.validateLine1(line1)?.let { put("line1", it) }
                        ShippingAddressValidation.validateDistrict(district)?.let { put("district", it) }
                        ShippingAddressValidation.validateCity(city)?.let { put("city", it) }
                        ShippingAddressValidation.validatePostalCode(postalCode, country)?.let { put("postalCode", it) }
                        ShippingAddressValidation.validateCountry(country)?.let { put("country", it) }
                    }
                    errors = newErrors
                    if (newErrors.isNotEmpty()) return@TextButton
                    val input = ShippingAddressInput(
                        label = label.trim(),
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
