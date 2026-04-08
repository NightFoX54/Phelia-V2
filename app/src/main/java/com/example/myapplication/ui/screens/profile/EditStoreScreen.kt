package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.StoreOwnerProfileViewModel

@Composable
fun EditStoreScreen(
    viewModel: StoreOwnerProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val store by viewModel.store.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    LaunchedEffect(store?.storeId, store?.name, store?.description, store?.logo) {
        store?.let {
            name = it.name
            description = it.description
            logoUrl = it.logo
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        AppTopBar(title = "Edit store", onBack = onBack, containerColor = Color.White)
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (store == null) {
                Text(
                    "Loading store…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                )
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Store name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                    minLines = 4,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Logo image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                    minLines = 2,
                    supportingText = { Text("Paste a direct image link (HTTPS).") },
                )

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodySmall)
                }
                if (success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Saved.", color = Color(0xFF16A34A), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        saving = true
                        errorText = null
                        success = false
                        viewModel.saveStore(name, description, logoUrl) { result ->
                            saving = false
                            result.fold(
                                onSuccess = { success = true },
                                onFailure = { e -> errorText = e.message ?: "Could not save" },
                            )
                        }
                    },
                    enabled = !saving && name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Text("Save changes")
                    }
                }
            }
        }
    }
}
