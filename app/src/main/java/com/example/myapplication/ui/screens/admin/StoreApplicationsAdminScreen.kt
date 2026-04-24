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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
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
    var updateTarget by remember { mutableStateOf<StoreApplication?>(null) }
    var detailTarget by remember { mutableStateOf<StoreApplication?>(null) }
    var actionReason by remember { mutableStateOf("") }
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
                            actionReason = ""
                            actionError = null
                        },
                        onRequestUpdate = {
                            updateTarget = app
                            actionReason = ""
                            actionError = null
                        },
                        onViewDetail = {
                            detailTarget = app
                        }
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
                        value = actionReason,
                        onValueChange = { actionReason = it },
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
                        viewModel.reject(app.applicationId, actionReason) { r ->
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

    updateTarget?.let { app ->
        AlertDialog(
            onDismissRequest = { if (busyId == null) updateTarget = null },
            title = { Text("Request Update") },
            text = {
                Column {
                    Text(
                        "Ask ${app.applicantName} to update their information. They will receive a notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        label = { Text("Message to applicant") },
                        placeholder = { Text("e.g. Please provide a clearer tax number.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = busyId == null,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionReason.isBlank()) {
                            actionError = "Please provide a reason for the update request."
                            return@Button
                        }
                        busyId = app.applicationId
                        viewModel.requestUpdate(app.applicationId, actionReason) { r ->
                            busyId = null
                            r.fold(
                                onSuccess = { updateTarget = null },
                                onFailure = { e -> actionError = e.message ?: "Request update failed" },
                            )
                        }
                    },
                    enabled = busyId == null,
                ) { Text("Send Request") }
            },
            dismissButton = {
                TextButton(
                    onClick = { updateTarget = null },
                    enabled = busyId == null,
                ) { Text("Cancel") }
            },
        )
    }

    detailTarget?.let { app ->
        AlertDialog(
            onDismissRequest = { detailTarget = null },
            title = { Text("Application Detail") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { DetailRow(Icons.Default.Person, "Applicant Name", app.applicantName) }
                    item { DetailRow(Icons.Default.Email, "Applicant Email", app.applicantEmail) }
                    item { DetailRow(Icons.Default.Phone, "Applicant Phone", app.applicantPhone.ifBlank { "N/A" }) }
                    item { Spacer(Modifier.height(4.dp)) }
                    item { DetailRow(Icons.Default.Business, "Store Name", app.storeName) }
                    item { DetailRow(Icons.Default.Info, "Tax Number", app.taxNumber.ifBlank { "N/A" }) }
                    item { DetailRow(Icons.Default.LocationOn, "Business Address", app.businessAddress.ifBlank { "N/A" }) }
                    item { DetailRow(Icons.Default.Description, "Store Description", app.storeDescription) }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailTarget = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6B7280))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StoreApplicationCard(
    application: StoreApplication,
    busy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestUpdate: () -> Unit,
    onViewDetail: () -> Unit,
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
                application.storeDescription.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF374151),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                onClick = onViewDetail,
                color = Color(0xFFF3F4F6),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "View Full Details & Tax Information",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !busy,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    Text(if (busy) "…" else "Approve", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onRequestUpdate,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6366F1)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    Text("Update", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onReject,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    Text("Reject", fontWeight = FontWeight.SemiBold)
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
