package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.InactiveProductAdminItem
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.ui.components.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun AdminInactiveProductsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository = remember { ProductRepository() }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<InactiveProductAdminItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var listError by remember { mutableStateOf<String?>(null) }
    var busyProductId by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadToken) {
        val blockUi = items.isEmpty()
        if (blockUi) loading = true
        listError = null
        repository.fetchInactiveProductsForAdmin().fold(
            onSuccess = {
                items = it
                listError = null
            },
            onFailure = {
                listError = it.message ?: "Liste yüklenemedi"
                items = emptyList()
            },
        )
        if (blockUi) loading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        AppTopBar(
            title = "Pasif ürünler",
            onBack = onBack,
            containerColor = Color.White,
        )
        if (banner != null) {
            Text(
                banner!!,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = Color(0xFF2563EB),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            loading && items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            listError != null -> {
                Text(
                    listError!!,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(20.dp),
                )
            }
            items.isEmpty() -> {
                Text(
                    "Pasif ürün yok.",
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFF6B7280),
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    items(items, key = { it.productId }) { row ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(row.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Mağaza: ${row.storeId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280),
                                    )
                                }
                                val busy = busyProductId == row.productId
                                Button(
                                    onClick = {
                                        scope.launch {
                                            busyProductId = row.productId
                                            repository.adminReactivateProduct(row.productId).fold(
                                                onSuccess = {
                                                    banner = "Ürün ve tüm varyantları tekrar aktif."
                                                    reloadToken++
                                                },
                                                onFailure = {
                                                    banner = it.message ?: "Aktifleştirilemedi"
                                                },
                                            )
                                            busyProductId = null
                                        }
                                    },
                                    enabled = !busy,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                ) {
                                    if (busy) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White,
                                        )
                                    } else {
                                        Text("Aktif et")
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.size(24.dp)) }
                }
            }
        }
    }
}
