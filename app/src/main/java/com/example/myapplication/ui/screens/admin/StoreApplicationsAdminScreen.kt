package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.StoreApplicationsViewModel

@Composable
fun StoreApplicationsAdminScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreApplicationsViewModel = viewModel(),
) {
    val applications by viewModel.applications.collectAsState()
    val listError by viewModel.listError.collectAsState()
    var rejectTarget by remember { mutableStateOf<StoreApplication?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var actionError by remember { mutableStateOf<String?>(null) }
    var busyId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        AppTopBar(title = "Store applications", onBack = onBack, containerColor = Color.White)
        listError?.let { err ->
            Text(
                err,
                color = Color(0xFFDC2626),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        actionError?.let { err ->
            Text(
                err,
                color = Color(0xFFDC2626),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            if (applications.isEmpty()) {
                item {
                    Text(
                        "No pending applications.",
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(applications, key = { it.applicationId }) { app ->
                    StoreApplicationCard(
                        application = app,
                        busy = busyId == app.applicationId,
                        onApprove = {
                            actionError = null
                            busyId = app.applicationId
                            viewModel.approve(app.applicationId) { r ->
                                busyId = null
                                r.onFailure { e -> actionError = e.message ?: "Approve failed" }
                            }
                        },
                        onReject = {
                            rejectTarget = app
                            rejectReason = ""
                            actionError = null
                        },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    rejectTarget?.let { app ->
        AlertDialog(
            onDismissRequest = { if (busyId == null) rejectTarget = null },
            title = { Text("Reject application") },
            text = {
                Column {
                    Text(
                        "Applicant: ${app.applicantEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = busyId == null,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        busyId = app.applicationId
                        viewModel.reject(app.applicationId, rejectReason) { r ->
                            busyId = null
                            r.fold(
                                onSuccess = { rejectTarget = null },
                                onFailure = { e -> actionError = e.message ?: "Reject failed" },
                            )
                        }
                    },
                    enabled = busyId == null,
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(
                    onClick = { rejectTarget = null },
                    enabled = busyId == null,
                ) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun StoreApplicationCard(
    application: StoreApplication,
    busy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (application.storeLogoUrl.isNotBlank()) {
                    AsyncImage(
                        model = application.storeLogoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    BoxGrayPlaceholder(Modifier.size(72.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        application.storeName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        application.applicantName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        application.applicantEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4338CA),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                application.storeDescription.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF374151),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                ) {
                    Text(if (busy) "…" else "Approve")
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
private fun BoxGrayPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center,
    ) {
        Text("No logo", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
    }
}
