package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.AdminUserManagementUiState
import com.example.myapplication.viewmodel.AdminUserManagementViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminUserManagementViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { androidx.compose.runtime.mutableStateOf("") }
    var roleFilter by remember { androidx.compose.runtime.mutableStateOf("all") }
    val users = (uiState as? AdminUserManagementUiState.Ready)?.users.orEmpty()
    val filteredUsers = users.filter { u ->
        (u.name.contains(searchQuery, true) || u.email.contains(searchQuery, true)) &&
            (
                roleFilter == "all" ||
                    (roleFilter == "store_owner" && u.role == "store_owner") ||
                    (roleFilter == "pending_store" && u.pendingStoreApplication) ||
                    roleFilter == u.role
                )
    }

    Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                AppTopBar(title = "User Management", onBack = onBack, containerColor = Color.White)
                Text("${filteredUsers.size} users", color = Color(0xFF6B7280), modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search users by name or email...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                ) {
                    items(
                        listOf(
                            "all" to "All Users",
                            "customer" to "Customers",
                            "store_owner" to "Store Owners",
                            "pending_store" to "Pending Store Applications",
                            "admin" to "Admins",
                        ),
                    ) { (key, label) ->
                        FilterChip(
                            selected = roleFilter == key,
                            onClick = { roleFilter = key },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        ) {
            if (uiState is AdminUserManagementUiState.Loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            if (uiState is AdminUserManagementUiState.Error) {
                val msg = (uiState as AdminUserManagementUiState.Error).message
                item {
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(msg, color = Color(0xFFDC2626))
                            TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
            }
            items(filteredUsers, key = { it.uid }) { user ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.size(46.dp).background(Color(0xFF6366F1), RoundedCornerShape(999.dp)), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text(user.name.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(user.name, fontWeight = FontWeight.SemiBold)
                                    Text(user.email, color = Color(0xFF6B7280), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                    Text("Joined ${formatJoinedDate(user.createdAtMs)}", color = Color(0xFF9CA3AF), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                    if (user.role == "store_owner" && !user.ownedStoreName.isNullOrBlank()) {
                                        Text(
                                            "Store: ${user.ownedStoreName}",
                                            color = Color(0xFF1D4ED8),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        )
                                    } else if (user.pendingStoreApplication) {
                                        val pendingFor = user.pendingStoreName?.let { " (${it})" }.orEmpty()
                                        Text(
                                            "Store application pending$pendingFor",
                                            color = Color(0xFF0D9488),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        RoleBadge(role = user.role, pendingStoreApplication = user.pendingStoreApplication)
                    }
                }
            }
            if (filteredUsers.isEmpty()) {
                item { Text("No users found", color = Color(0xFF6B7280), modifier = Modifier.padding(20.dp)) }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String, pendingStoreApplication: Boolean) {
    val (bg, fg, label) = when (role) {
        "admin" -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), "Admin")
        "store_owner" -> if (pendingStoreApplication) {
            Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), "Store Owner (Pending approval)")
        } else {
            Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "Store Owner")
        }
        else -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Customer")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(label, color = fg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatJoinedDate(ms: Long): String {
    if (ms <= 0L) return "—"
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ms))
    }.getOrDefault("—")
}

