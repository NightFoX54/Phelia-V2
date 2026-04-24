package com.example.myapplication.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.StoreOwnerProfileViewModel

@Composable
fun EditStoreScreen(
    viewModel: StoreOwnerProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val store by viewModel.store.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var uploadLogoBusy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val pickLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        uploadLogoBusy = true
        errorText = null
        success = false
        viewModel.uploadStoreLogoFromGallery(context, uri) { result ->
            uploadLogoBusy = false
            result.fold(
                onSuccess = { logoUrl = it },
                onFailure = { e -> errorText = e.message ?: "Could not upload logo" },
            )
        }
    }

    LaunchedEffect(store?.storeId, store?.name, store?.description, store?.logo, store?.email, store?.phone, store?.taxNumber, store?.businessAddress) {
        store?.let {
            name = it.name
            description = it.description
            logoUrl = it.logo
            email = it.email
            phone = it.phone
            taxNumber = it.taxNumber
            businessAddress = it.businessAddress
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
                    enabled = !saving && !uploadLogoBusy,
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    minLines = 4,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = taxNumber,
                    onValueChange = { taxNumber = it },
                    label = { Text("Tax Number") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = businessAddress,
                    onValueChange = { businessAddress = it },
                    label = { Text("Business Address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    minLines = 2,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Store logo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Photos are center-cropped to a square (1:1), then saved to your store folder in Firebase Storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Store logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        pickLogoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = !saving && !uploadLogoBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uploadLogoBusy) "Uploading…" else "Choose logo from gallery")
                }
                if (uploadLogoBusy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Logo URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving && !uploadLogoBusy,
                    minLines = 2,
                    supportingText = {
                        Text("Override or paste a direct HTTPS link if you host the image elsewhere.")
                    },
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
                Text(
                    "Note: Submitting these changes will send them to an administrator for approval. Your store details will update once approved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Button(
                    onClick = {
                        saving = true
                        errorText = null
                        success = false
                        viewModel.submitUpdateStore(
                            name = name,
                            description = description,
                            logoUrl = logoUrl,
                            email = email,
                            phone = phone,
                            taxNumber = taxNumber,
                            businessAddress = businessAddress
                        ) { result ->
                            saving = false
                            result.fold(
                                onSuccess = { success = true },
                                onFailure = { e -> errorText = e.message ?: "Could not submit update" },
                            )
                        }
                    },
                    enabled = !saving && !uploadLogoBusy && name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Text("Submit for Approval")
                    }
                }
            }
        }
    }
}
