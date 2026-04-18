package com.example.myapplication.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.data.model.StoreWeeklySalesSummary
import com.example.myapplication.viewmodel.StoreProductsLoadState
import com.example.myapplication.viewmodel.StoreProductsViewModel
import com.example.myapplication.viewmodel.StoreWeeklySalesLoadState
import java.util.Locale

@Composable
fun StoreDashboardScreen(
    storeProductsViewModel: StoreProductsViewModel,
    onAddProduct: () -> Unit,
    onOpenProductDetail: (String) -> Unit,
    onEditProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerGradient = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED)))
    val rows by storeProductsViewModel.rows.collectAsState()
    val loadState by storeProductsViewModel.loadState.collectAsState()
    val weeklySales by storeProductsViewModel.weeklySales.collectAsState()
    val salesRangeDays by storeProductsViewModel.salesRangeDays.collectAsState()
    val loadSt = loadState

    val totalStock = rows.sumOf { it.totalStock }
    val totalReviews = rows.sumOf { it.reviewCount }
    val totalVariants = rows.sumOf { it.variantCount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(headerGradient)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Store", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Store Dashboard", color = Color.White.copy(alpha = 0.85f))
                        }
                        Button(
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Add", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Default.Inventory, "Products", rows.size.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.TrendingUp, "In stock", totalStock.toString(), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Default.Star, "Reviews", totalReviews.toString(), modifier = Modifier.weight(1f))
                        StatCard(Icons.Default.Inventory, "Variants", totalVariants.toString(), modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                SalesThisWeekSection(
                    state = weeklySales,
                    currentRange = salesRangeDays,
                    onRangeChange = { storeProductsViewModel.refreshWeeklySales(it) },
                    onRetry = { storeProductsViewModel.refreshWeeklySales() },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Products", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${rows.size} items", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (loadSt) {
                StoreProductsLoadState.Idle, StoreProductsLoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
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
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                is StoreProductsLoadState.Error -> {
                    item {
                        Text(
                            loadSt.message,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = Color(0xFFDC2626),
                        )
                    }
                }
                StoreProductsLoadState.Ready -> {
                    if (rows.isEmpty()) {
                        item {
                            Text(
                                "No products yet. Tap Add to create one.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                color = Color(0xFF6B7280),
                            )
                        }
                    } else {
                        items(rows, key = { it.productId }) { product ->
                            DashboardProductCard(
                                product = product,
                                onOpenDetail = { onOpenProductDetail(product.productId) },
                                onEditProduct = onEditProduct,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        FloatingActionButton(
            onClick = onAddProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
private fun DashboardProductCard(
    product: StoreOwnerProductRow,
    onOpenDetail: () -> Unit,
    onEditProduct: (String) -> Unit,
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
                        contentDescription = product.name,
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
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            if (!product.isActive) {
                                Text(
                                    "INACTIVE",
                                    color = Color(0xFFDC2626),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF)) }
                    }
                    Text(
                        "$" + String.format(Locale.US, "%.2f", product.minPrice),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("${product.reviewCount}", "Reviews", Color(0xFFEFF6FF), Color(0xFF2563EB), Modifier.weight(1f))
                        MiniStat("${product.totalStock}", "Stock", Color(0xFFFFF7ED), Color(0xFFEA580C), Modifier.weight(1f))
                        MiniStat("${product.variantCount}", "SKU", Color(0xFFF0FDF4), Color(0xFF16A34A), Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEditProduct(product.productId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Edit")
                }
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(value, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(label, color = fg.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SalesThisWeekSection(
    state: StoreWeeklySalesLoadState,
    currentRange: Int,
    onRangeChange: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val rangeOptions = listOf(
        1 to "Last Day",
        7 to "Last 7 Days",
        15 to "Last 15 Days",
        30 to "Last Month",
        90 to "Last 3 Months",
        180 to "Last 6 Months",
        365 to "Last Year"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sales Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                    )
                    Text(
                        "Revenue from your store packages",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                }
                Box {
                    Surface(
                        onClick = { expanded = true },
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = rangeOptions.find { it.first == currentRange }?.second ?: "Last 7 Days",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        rangeOptions.forEach { (days, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onRangeChange(days)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            when (state) {
                StoreWeeklySalesLoadState.Idle,
                StoreWeeklySalesLoadState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE5E7EB),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading sales…", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF))
                }
                is StoreWeeklySalesLoadState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(state.message, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodySmall)
                    Text(
                        "If this is the first time, create the Firestore composite index for collection group \"suborders\" (storeId + createdAt).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    TextButton(onClick = onRetry, modifier = Modifier.padding(top = 4.dp)) { Text("Retry") }
                }
                is StoreWeeklySalesLoadState.Ready -> {
                    SalesThisWeekContent(summary = state.summary)
                }
            }
        }
    }
}

@Composable
private fun SalesThisWeekContent(summary: StoreWeeklySalesSummary) {
    val accent = MaterialTheme.colorScheme.primary
    val accentSoft = Color(0xFFEEF2FF)
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        summary.rangeLabel,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF6B7280),
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = accentSoft,
            modifier = Modifier.weight(1f),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Total revenue", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4338CA))
                Text(
                    "$" + String.format(Locale.US, "%.2f", summary.weekTotalRevenue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B),
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF0FDF4),
            modifier = Modifier.weight(1f),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Packages", style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D))
                Text(
                    summary.weekSuborderCount.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14532D),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
    val maxRev = summary.days.maxOf { it.revenue }.coerceAtLeast(1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .then(if (summary.days.size > 7) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
        horizontalArrangement = if (summary.days.size > 7) Arrangement.spacedBy(12.dp) else Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        summary.days.forEach { day ->
            Column(
                modifier = if (summary.days.size > 7) Modifier.width(42.dp) else Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val barFrac = (day.revenue / maxRev).toFloat().coerceIn(0f, 1f)
                val barH = maxOf(4.dp, 100.dp * barFrac)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (summary.days.size > 15) 16.dp else 26.dp)
                            .height(barH)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.55f)),
                                ),
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "$" + String.format(Locale.US, "%.0f", day.revenue),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = Color(0xFFF3F4F6))
    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Day", modifier = Modifier.weight(0.22f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        Text("Revenue", modifier = Modifier.weight(0.38f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        Text("Orders", modifier = Modifier.weight(0.2f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
    Spacer(modifier = Modifier.height(6.dp))
    summary.days.forEachIndexed { index, day ->
        if (index > 0) HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(day.label, modifier = Modifier.weight(0.22f), style = MaterialTheme.typography.bodySmall, color = Color(0xFF374151))
            Text(
                "$" + String.format(Locale.US, "%.2f", day.revenue),
                modifier = Modifier.weight(0.38f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                day.suborderCount.toString(),
                modifier = Modifier.weight(0.2f),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
            )
        }
    }
}
