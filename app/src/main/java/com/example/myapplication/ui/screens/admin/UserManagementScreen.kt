package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppTopBar

private data class AdminUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val joinDate: String,
)

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialUsers = remember {
        mutableListOf(
            AdminUser("1", "John Doe", "user@test.com", "customer", "2024-01-15"),
            AdminUser("2", "Store Owner", "store@test.com", "store", "2024-02-10"),
            AdminUser("3", "Admin User", "admin@test.com", "admin", "2024-01-01"),
            AdminUser("4", "Sarah Johnson", "sarah.j@email.com", "customer", "2024-03-05"),
            AdminUser("5", "Mike's Electronics", "mike@store.com", "store", "2024-02-20"),
            AdminUser("6", "Emily Chen", "emily.chen@email.com", "customer", "2024-03-12"),
            AdminUser("7", "TechMart Store", "contact@techmart.com", "store", "2024-01-25"),
        )
    }
    var users by remember { mutableStateOf(initialUsers.toList()) }
    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("all") }
    var activeMenuId by remember { mutableStateOf<String?>(null) }

    val filteredUsers = users.filter {
        (it.name.contains(searchQuery, true) || it.email.contains(searchQuery, true)) &&
            (roleFilter == "all" || roleFilter == it.role)
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
                    items(listOf("all" to "All Users", "customer" to "Customers", "store" to "Store Owners", "admin" to "Admins")) { (key, label) ->
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
            items(filteredUsers, key = { it.id }) { user ->
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
                                    Text("Joined ${user.joinDate}", color = Color(0xFF9CA3AF), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                }
                            }
                            Box {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.clickable { activeMenuId = if (activeMenuId == user.id) null else user.id },
                                )
                                DropdownMenu(expanded = activeMenuId == user.id, onDismissRequest = { activeMenuId = null }) {
                                    DropdownMenuItem(
                                        text = { Text("Set as Customer") },
                                        onClick = {
                                            users = users.map { if (it.id == user.id) it.copy(role = "customer") else it }
                                            activeMenuId = null
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Set as Store Owner") },
                                        onClick = {
                                            users = users.map { if (it.id == user.id) it.copy(role = "store") else it }
                                            activeMenuId = null
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Set as Admin") },
                                        onClick = {
                                            users = users.map { if (it.id == user.id) it.copy(role = "admin") else it }
                                            activeMenuId = null
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete User", color = Color(0xFFDC2626)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626)) },
                                        onClick = {
                                            users = users.filterNot { it.id == user.id }
                                            activeMenuId = null
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        RoleBadge(user.role)
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
private fun RoleBadge(role: String) {
    val (bg, fg, label) = when (role) {
        "admin" -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), "Admin")
        "store" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "Store Owner")
        else -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Customer")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(label, color = fg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

