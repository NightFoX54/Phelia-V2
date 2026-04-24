package com.example.myapplication.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.data.repository.StoreApplicationRepository
import com.example.myapplication.ui.components.AppTopBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreApplicationRetryViewModel(
    private val repository: StoreApplicationRepository = StoreApplicationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _uiState = MutableStateFlow<RetryUiState>(RetryUiState.Loading)
    val uiState: StateFlow<RetryUiState> = _uiState.asStateFlow()

    init {
        loadApplication()
    }

    private fun loadApplication() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getApplicationForUser(uid).fold(
                onSuccess = { app ->
                    if (app != null) {
                        _uiState.value = RetryUiState.Loaded(app)
                    } else {
                        _uiState.value = RetryUiState.Error("No application found.")
                    }
                },
                onFailure = { _uiState.value = RetryUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resubmit(
        context: android.content.Context,
        applicationId: String,
        storeName: String,
        storeDescription: String,
        newLogoUri: Uri?,
        currentLogoUrl: String,
        taxNumber: String,
        businessAddress: String,
        applicantPhone: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val logoUrl = if (newLogoUri != null) {
                repository.uploadApplicantStoreLogo(context, auth.currentUser?.uid.orEmpty(), newLogoUri).getOrElse {
                    onResult(Result.failure(it))
                    return@launch
                }
            } else {
                currentLogoUrl
            }

            val result = repository.resubmitApplication(
                applicationId = applicationId,
                storeName = storeName,
                storeDescription = storeDescription,
                storeLogoUrl = logoUrl,
                taxNumber = taxNumber,
                businessAddress = businessAddress,
                applicantPhone = applicantPhone
            )
            onResult(result)
        }
    }
}

sealed class RetryUiState {
    object Loading : RetryUiState()
    data class Loaded(val application: StoreApplication) : RetryUiState()
    data class Error(val message: String) : RetryUiState()
}

@Composable
fun StoreApplicationRetryScreen(
    onBack: () -> Unit,
    viewModel: StoreApplicationRetryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Retry Application", onBack = onBack) },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is RetryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is RetryUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is RetryUiState.Loaded -> RetryForm(state.application, viewModel, onBack)
            }
        }
    }
}

@Composable
private fun RetryForm(
    app: StoreApplication,
    viewModel: StoreApplicationRetryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var storeName by remember { mutableStateOf(app.storeName) }
    var storeDescription by remember { mutableStateOf(app.storeDescription) }
    var taxNumber by remember { mutableStateOf(app.taxNumber) }
    var businessAddress by remember { mutableStateOf(app.businessAddress) }
    var phone by remember { mutableStateOf(app.applicantPhone) }
    var newLogoUri by remember { mutableStateOf<Uri?>(null) }
    
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) newLogoUri = uri
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (app.rejectionReason.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rejection Reason", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(app.rejectionReason, color = Color(0xFFB91C1C))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taxNumber,
                        onValueChange = { taxNumber = it },
                        label = { Text("Tax Number") },
                        modifier = Modifier.weight(1f),
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = businessAddress,
                    onValueChange = { businessAddress = it },
                    label = { Text("Business Address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = storeDescription,
                    onValueChange = { storeDescription = it },
                    label = { Text("Store Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Store Logo", fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                        .clickable(enabled = !busy) {
                            pickLogo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val displayUri = newLogoUri ?: if (app.storeLogoUrl.isNotBlank()) app.storeLogoUrl.toUri() else null
                    if (displayUri != null) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                }
                Text("Tap to change logo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                if (storeName.isBlank() || taxNumber.isBlank() || phone.isBlank()) {
                    error = "Please fill in all required fields."
                    return@Button
                }
                busy = true
                viewModel.resubmit(
                    context = context,
                    applicationId = app.applicationId,
                    storeName = storeName,
                    storeDescription = storeDescription,
                    newLogoUri = newLogoUri,
                    currentLogoUrl = app.storeLogoUrl,
                    taxNumber = taxNumber,
                    businessAddress = businessAddress,
                    applicantPhone = phone
                ) { result ->
                    busy = false
                    result.fold(
                        onSuccess = { onBack() },
                        onFailure = { error = it.message }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !busy
        ) {
            if (busy) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Resubmit Application")
            }
        }
    }
}
