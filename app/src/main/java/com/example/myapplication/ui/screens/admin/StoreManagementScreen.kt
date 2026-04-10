package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.AdminStoreListUiState
import com.example.myapplication.viewmodel.AdminStoreManagementViewModel
import java.util.Locale

@Composable
fun StoreManagementScreen(
    onBack: () -> Unit,
    onOpenStore: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminStoreManagementViewModel = viewModel(),
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val stores = (uiState as? AdminStoreListUiState.Ready)?.stores.orEmpty()
    val filteredStores = stores.filter {
        it.name.contains(searchQuery, true) ||
            it.ownerName.contains(searchQuery, true) ||
            it.ownerEmail.contains(searchQuery, true)
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                AppTopBar(title = "Store Management", onBack = onBack, containerColor = Color.White)
                Text("${filteredStores.size} stores", color = Color(0xFF6B7280), modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search stores...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState is AdminStoreListUiState.Loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) { CircularProgressIndicator() }
                }
            }
            if (uiState is AdminStoreListUiState.Error) {
                val msg = (uiState as AdminStoreListUiState.Error).message
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(msg, color = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            items(filteredStores, key = { it.storeId }) { store ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(store.name, fontWeight = FontWeight.Bold)
                                }
                                Text(store.ownerName, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                Text(store.ownerEmail, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Text(String.format("%.1f", store.rating), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StatTile("Products", store.totalProducts.toString(), Color(0xFFEFF6FF), Color(0xFF2563EB), Modifier.weight(1f))
                            StatTile("Orders", store.totalOrders.toString(), Color(0xFFF0FDF4), Color(0xFF16A34A), Modifier.weight(1f))
                            StatTile("Sales", formatCompactCurrency(store.totalSales), Color(0xFFF5F3FF), Color(0xFF6D28D9), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Joined ${store.joinDate}", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onOpenStore(store.storeId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("View Store")
                            }
                        }
                    }
                }
            }
            if (filteredStores.isEmpty()) {
                item { Text("No stores found", color = Color(0xFF6B7280), modifier = Modifier.padding(20.dp)) }
            }
        }
    }
}

private fun formatCompactCurrency(value: Double): String = when {
    value >= 1_000_000.0 -> "$" + String.format(Locale.US, "%.2fM", value / 1_000_000.0)
    value >= 1_000.0 -> "$" + String.format(Locale.US, "%.1fK", value / 1_000.0)
    else -> "$" + String.format(Locale.US, "%.0f", value)
}

@Composable
private fun StatTile(label: String, value: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
            Text(value, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

