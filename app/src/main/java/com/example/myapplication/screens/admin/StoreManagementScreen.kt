package com.example.myapplication.screens.admin

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.AppTopBar

private data class StoreItem(
    val id: String,
    val name: String,
    val ownerName: String,
    val ownerEmail: String,
    val rating: Double,
    val totalSales: String,
    val totalProducts: Int,
    val totalOrders: Int,
    val status: String,
    val joinDate: String,
)

@Composable
fun StoreManagementScreen(
    onBack: () -> Unit,
    onOpenStore: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var stores by remember {
        mutableStateOf(
            listOf(
                StoreItem("1", "TechStore Pro", "Store Owner", "store@test.com", 4.9, "$2,548,000", 145, 1250, "active", "2023-06-15"),
                StoreItem("2", "Gadget Hub", "Mike Anderson", "mike@store.com", 4.8, "$1,823,500", 98, 890, "active", "2023-08-20"),
                StoreItem("3", "ElectroWorld", "Sarah Martinez", "sarah@electroworld.com", 4.7, "$1,567,200", 112, 720, "active", "2023-07-10"),
                StoreItem("4", "TechMart", "David Lee", "contact@techmart.com", 4.5, "$987,400", 76, 450, "active", "2023-09-05"),
                StoreItem("5", "SmartDevices Co", "Emma Wilson", "emma@smartdevices.com", 3.8, "$245,600", 34, 120, "disabled", "2024-01-15"),
            ),
        )
    }
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
            items(filteredStores, key = { it.id }) { store ->
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
                                    if (store.status == "disabled") {
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("Disabled", color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodySmall)
                                    }
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
                            StatTile("Sales", store.totalSales, Color(0xFFF5F3FF), Color(0xFF6D28D9), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Joined ${store.joinDate}", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onOpenStore(store.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("View Store")
                            }
                            Button(
                                onClick = {
                                    stores = stores.map {
                                        if (it.id == store.id) it.copy(status = if (it.status == "active") "disabled" else "active") else it
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (store.status == "active") Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                    contentColor = if (store.status == "active") Color(0xFFDC2626) else Color(0xFF16A34A),
                                ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Block, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(if (store.status == "active") "Disable" else "Enable")
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

@Composable
private fun StatTile(label: String, value: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
            Text(value, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

