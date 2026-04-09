package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.viewmodel.StoreProductsLoadState
import com.example.myapplication.viewmodel.StoreProductsViewModel
import java.util.Locale

@Composable
fun StoreProductsScreen(
    viewModel: StoreProductsViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onOpenProductDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var confirmDeactivateId by remember { mutableStateOf<String?>(null) }
    val rows by viewModel.rows.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    LaunchedEffect(userMessage) {
        if (userMessage == null) return@LaunchedEffect
        delay(5_000)
        viewModel.clearUserMessage()
    }
    val filtered = remember(rows, searchQuery) {
        rows.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val st = loadState
    confirmDeactivateId?.let { pid ->
        AlertDialog(
            onDismissRequest = { confirmDeactivateId = null },
            title = { Text("Remove from sale") },
            text = {
                Text("The product is not deleted; it is only deactivated. Past orders stay the same; customers will not see it.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deactivateProduct(pid)
                        confirmDeactivateId = null
                    },
                ) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeactivateId = null }) { Text("Cancel") }
            },
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        LazyColumn {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED))))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Products", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("${rows.size} products", color = Color.White.copy(alpha = 0.85f))
                        }
                        Button(
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Add")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFC7D2FE)) },
                        trailingIcon = { IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null, tint = Color.White) } },
                        placeholder = { Text("Search products...", color = Color(0xFFC7D2FE)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedBorderColor = Color.White.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                }
            }

            if (userMessage != null) {
                item {
                    Text(
                        userMessage!!,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = Color(0xFF2563EB),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            when (st) {
                StoreProductsLoadState.Idle, StoreProductsLoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                StoreProductsLoadState.NoStore -> {
                    item {
                        Text(
                            "No store linked to this account.",
                            modifier = Modifier.padding(24.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                is StoreProductsLoadState.Error -> {
                    item {
                        Text(
                            st.message,
                            modifier = Modifier.padding(24.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                StoreProductsLoadState.Ready -> {
                    if (filtered.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(modifier = Modifier.size(80.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No products yet", style = MaterialTheme.typography.titleMedium)
                                Text("Add a product or adjust search.", color = Color(0xFF6B7280))
                            }
                        }
                    } else {
                        items(filtered, key = { it.productId }) { product ->
                            StoreProductListCard(
                                product = product,
                                onOpenDetail = { onOpenProductDetail(product.productId) },
                                onEdit = { onEditProduct(product.productId) },
                                onRequestDeactivate = { confirmDeactivateId = product.productId },
                                onActivate = { viewModel.activateProduct(product.productId) },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAddProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
        ) { Icon(Icons.Default.Add, null) }
    }
}

@Composable
private fun StoreProductListCard(
    product: StoreOwnerProductRow,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onRequestDeactivate: () -> Unit,
    onActivate: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable(onClick = onOpenDetail),
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = Color(0xFF9CA3AF))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CategoryBadge(product.categoryName)
                                if (!product.isActive) {
                                    Text(
                                        "INACTIVE",
                                        color = Color(0xFFDC2626),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "$" + String.format(Locale.US, "%.2f", product.minPrice),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("${product.variantCount}", "Variants", Modifier.weight(1f), Color(0xFFEFF6FF), Color(0xFF2563EB))
                        MiniStat("${product.totalStock}", "Stock", Modifier.weight(1f), Color(0xFFFFF7ED), Color(0xFFEA580C))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Edit")
                }
                if (product.isActive) {
                    Button(
                        onClick = onRequestDeactivate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Remove from sale")
                    }
                } else {
                    Button(
                        onClick = onActivate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0FDF4), contentColor = Color(0xFF16A34A)),
                    ) {
                        Text("Activate again")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(text: String) {
    Box(modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(text, color = Color(0xFF4338CA), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier, bg: Color, fg: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(value, color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
        }
    }
}
