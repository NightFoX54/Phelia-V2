package com.example.myapplication.ui.screens.store

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.myapplication.navigation.AppRoutes
import com.example.myapplication.data.model.StoreApplication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.StoreOwnerProductRow
import com.example.myapplication.data.model.StoreWeeklySalesSummary
import com.example.myapplication.data.repository.UserNotificationItem
import com.example.myapplication.viewmodel.StoreProductsLoadState
import com.example.myapplication.viewmodel.StoreProductsViewModel
import com.example.myapplication.viewmodel.StoreWeeklySalesLoadState
import com.example.myapplication.viewmodel.UserSettingsViewModel
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoreDashboardScreen(
    storeProductsViewModel: StoreProductsViewModel,
    userSettingsViewModel: UserSettingsViewModel,
    onAddProduct: () -> Unit,
    onOpenProductDetail: (String) -> Unit,
    onEditProduct: (String) -> Unit,
    onNavigateToRetry: () -> Unit,
    onOpenNotifications: () -> Unit,
    onNavigateToStoreProducts: () -> Unit = {},
    onNavigateToStoreOrders: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val headerGradient = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED)))
    val rows by storeProductsViewModel.rows.collectAsState()
    val loadState by storeProductsViewModel.loadState.collectAsState()
    val weeklySales by storeProductsViewModel.weeklySales.collectAsState()
    val salesRangeDays by storeProductsViewModel.salesRangeDays.collectAsState()
    val notifications by userSettingsViewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val loadSt = loadState

    val totalStock = rows.sumOf { it.totalStock }
    val totalReviews = rows.sumOf { it.reviewCount }
    val totalVariants = rows.sumOf { it.variantCount }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var confirmDeleteProductId by remember { mutableStateOf<String?>(null) }
    val userMessage by storeProductsViewModel.userMessage.collectAsState()
    LaunchedEffect(userMessage) {
        if (userMessage == null) return@LaunchedEffect
        delay(5_000)
        storeProductsViewModel.clearUserMessage()
    }
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 160
        }
    }

    confirmDeleteProductId?.let { pid ->
        AlertDialog(
            onDismissRequest = { confirmDeleteProductId = null },
            title = { Text("Delete product?") },
            text = {
                Text(
                    "If this product isn’t in any open orders or shopping carts, it will be removed permanently. " +
                        "Otherwise it will only be hidden from your storefront until orders finish.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storeProductsViewModel.deleteProduct(pid)
                        confirmDeleteProductId = null
                    },
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteProductId = null }) { Text("Cancel") }
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(headerGradient)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Store", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Store Dashboard", color = Color.White.copy(alpha = 0.85f))
                        }
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = Color.Red, contentColor = Color.White) {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                )
                            }
                        }
                        if (loadSt is StoreProductsLoadState.Ready) {
                            Button(
                                onClick = onAddProduct,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Add", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (loadSt is StoreProductsLoadState.Ready) {
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
                    } else if (loadSt is StoreProductsLoadState.NoStore) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Complete your store application to start selling products and viewing statistics.",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (loadSt is StoreProductsLoadState.Ready && userMessage != null) {
                item {
                    Text(
                        userMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
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
                Surface(
                    onClick = onNavigateToStoreProducts,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Products",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${rows.size} items →",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
                is StoreProductsLoadState.NoStore -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            
                            val statusText = when (loadSt.applicationStatus) {
                                StoreApplication.STATUS_PENDING -> "Your account is ready, you will be notified when your store is approved."
                                StoreApplication.STATUS_UPDATE_REQUESTED -> "Your application needs some updates. Please review and resubmit."
                                StoreApplication.STATUS_REJECTED -> "Your application was unfortunately rejected. You can view details and try again."
                                else -> "No store is linked to this account."
                            }
                            
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            
                            if (loadSt.applicationStatus == StoreApplication.STATUS_UPDATE_REQUESTED || 
                                loadSt.applicationStatus == StoreApplication.STATUS_REJECTED) {
                                Button(
                                    onClick = onNavigateToRetry,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("View Application")
                                }
                            }
                        }
                    }
                }
                is StoreProductsLoadState.Error -> {
                    item {
                        Text(
                            loadSt.message,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                StoreProductsLoadState.Ready -> {
                    if (rows.isEmpty()) {
                        item {
                            Text(
                                "No products yet. Tap Add to create one.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(rows, key = { it.productId }) { product ->
                            DashboardProductCard(
                                product = product,
                                onOpenDetail = { onOpenProductDetail(product.productId) },
                                onEditProduct = onEditProduct,
                                onRequestDelete = { confirmDeleteProductId = product.productId },
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        if (showScrollToTop) {
            FloatingActionButton(
                onClick = { scope.launch { listState.scrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 156.dp),
            ) {
                Icon(Icons.Default.ExpandLess, contentDescription = "Scroll to top")
            }
        }
        if (loadSt is StoreProductsLoadState.Ready) {
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
}

@Composable
private fun DashboardProductCard(
    product: StoreOwnerProductRow,
    onOpenDetail: () -> Unit,
    onEditProduct: (String) -> Unit,
    onRequestDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!product.isActive) {
                        Text(
                            "INACTIVE",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        "$" + String.format(Locale.US, "%.2f", product.minPrice),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val scheme = MaterialTheme.colorScheme
                        MiniStat(
                            "${product.reviewCount}",
                            "Reviews",
                            scheme.primaryContainer,
                            scheme.onPrimaryContainer,
                            Modifier.weight(1f),
                        )
                        MiniStat(
                            "${product.totalStock}",
                            "Stock",
                            scheme.secondaryContainer,
                            scheme.onSecondaryContainer,
                            Modifier.weight(1f),
                        )
                        MiniStat(
                            "${product.variantCount}",
                            "SKU",
                            scheme.tertiaryContainer,
                            scheme.onTertiaryContainer,
                            Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEditProduct(product.productId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onRequestDelete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Revenue from your store packages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    Surface(
                        onClick = { expanded = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading sales…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                is StoreWeeklySalesLoadState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "If this is the first time, create the Firestore composite index for collection group \"suborders\" (storeId + createdAt).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val scheme = MaterialTheme.colorScheme
    val accent = scheme.primary
    val accentSoft = scheme.primaryContainer
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        summary.rangeLabel,
        style = MaterialTheme.typography.labelMedium,
        color = scheme.onSurfaceVariant,
    )
    val days = summary.days
    val nonZeroDays = days.filter { it.revenue > 0.0 }
    val avgPerDay = if (days.isEmpty()) 0.0 else summary.weekTotalRevenue / days.size.toDouble()
    val best = nonZeroDays.maxByOrNull { it.revenue }
    val firstHalf = days.take(maxOf(1, days.size / 2)).sumOf { it.revenue }
    val secondHalf = days.drop(days.size / 2).sumOf { it.revenue }
    val trendUp = secondHalf >= firstHalf
    val longBreakdown = summary.days.size > 10
    var breakdownExpanded by remember(summary.days.size, summary.rangeLabel) { mutableStateOf(false) }
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
                Text("Total revenue", style = MaterialTheme.typography.labelSmall, color = scheme.onPrimaryContainer)
                Text(
                    "$" + String.format(Locale.US, "%.2f", summary.weekTotalRevenue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                )
                Text(
                    "Avg/day $" + String.format(Locale.US, "%.2f", avgPerDay),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.secondaryContainer,
            modifier = Modifier.weight(1f),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Packages", style = MaterialTheme.typography.labelSmall, color = scheme.onSecondaryContainer)
                Text(
                    summary.weekSuborderCount.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondaryContainer,
                )
                val arrow = if (trendUp) "▲" else "▼"
                val trendColor = if (trendUp) scheme.tertiary else scheme.error
                Text(
                    "$arrow Trend",
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
    val maxRev = summary.days.maxOf { it.revenue }.coerceAtLeast(1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (summary.days.size > 7) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            .padding(bottom = 10.dp),
        horizontalArrangement = if (summary.days.size > 7) Arrangement.spacedBy(12.dp) else Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        summary.days.forEach { day ->
            Column(
                modifier = Modifier
                    .then(if (summary.days.size > 7) Modifier.width(46.dp) else Modifier.weight(1f))
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val barFrac = (day.revenue / maxRev).toFloat().coerceIn(0f, 1f)
                val barH = maxOf(4.dp, 100.dp * barFrac)
                val isBest = best?.label == day.label && best.revenue == day.revenue && day.revenue > 0.0
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
                                    if (isBest) {
                                        listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
                                    } else {
                                        listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.55f))
                                    },
                                ),
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "$" + String.format(Locale.US, "%.0f", day.revenue),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                "Day",
                modifier = Modifier.weight(0.22f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Revenue",
                modifier = Modifier.weight(0.38f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Orders",
                modifier = Modifier.weight(0.2f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    val visibleDays = if (!longBreakdown || breakdownExpanded) summary.days else summary.days.take(7)
    visibleDays.forEachIndexed { index, day ->
        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(day.label, modifier = Modifier.weight(0.22f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (longBreakdown) {
        TextButton(
            onClick = { breakdownExpanded = !breakdownExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (breakdownExpanded) "Show less"
                else "Show all ${summary.days.size} days · revenue breakdown",
            )
        }
    }
}
