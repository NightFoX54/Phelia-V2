package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Store
import com.example.myapplication.navigation.AppRoutes
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.OrderHistoryViewModel
import com.example.myapplication.viewmodel.SessionViewModel
import com.example.myapplication.viewmodel.StoreOwnerProfileViewModel
import java.util.Locale

@Composable
fun StoreOwnerProfileScreen(
    sessionViewModel: SessionViewModel,
    storeOwnerProfileViewModel: StoreOwnerProfileViewModel,
    orderHistoryViewModel: OrderHistoryViewModel,
    favoritesViewModel: FavoritesViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val store by storeOwnerProfileViewModel.store.collectAsState()
    val loadError by storeOwnerProfileViewModel.storeLoadError.collectAsState()

    val orders by orderHistoryViewModel.orders.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()
    val orderCountText = orders.size.toString()
    val favoritesCountText = favoriteIds.size.toString()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            StoreOwnerHeader(
                store = store,
                loadError = loadError,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (store != null) {
                    ProfileQuickCard(
                        icon = Icons.Default.Dashboard,
                        iconBg = Color(0xFFE0E7FF),
                        iconTint = MaterialTheme.colorScheme.primary,
                        value = null,
                        label = "Dashboard",
                        onClick = { onNavigate(AppRoutes.STORE_DASHBOARD) },
                        modifier = Modifier.weight(1f),
                    )
                    ProfileQuickCard(
                        icon = Icons.Default.Inventory,
                        iconBg = Color(0xFFDCFCE7),
                        iconTint = Color(0xFF16A34A),
                        value = null,
                        label = "Products",
                        onClick = { onNavigate(AppRoutes.STORE_PRODUCTS) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    ProfileQuickCard(
                        icon = Icons.Default.ShoppingBag,
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = Color(0xFF9333EA),
                        value = orderCountText,
                        label = "My Orders",
                        onClick = { onNavigate(AppRoutes.profileOrders()) },
                        modifier = Modifier.weight(1f),
                    )
                    ProfileQuickCard(
                        icon = Icons.Default.Favorite,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFEF4444),
                        value = favoritesCountText,
                        label = "Favorites",
                        onClick = { onNavigate(AppRoutes.PROFILE_FAVORITES) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (store != null) {
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileQuickCard(
                        icon = Icons.Default.ShoppingBag,
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = Color(0xFF9333EA),
                        value = orderCountText,
                        label = "My Orders",
                        onClick = { onNavigate(AppRoutes.profileOrders()) },
                        modifier = Modifier.weight(1f),
                    )
                    ProfileQuickCard(
                        icon = Icons.Default.Favorite,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFEF4444),
                        value = favoritesCountText,
                        label = "Favorites",
                        onClick = { onNavigate(AppRoutes.PROFILE_FAVORITES) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column {
                    if (store != null) {
                        Text(
                            "Store Management",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        ProfileMenuRow(
                            icon = Icons.Default.Edit,
                            label = "Edit store details",
                            tint = MaterialTheme.colorScheme.primary,
                        ) { onNavigate(AppRoutes.STORE_PROFILE_EDIT) }
                        ProfileMenuRow(
                            icon = Icons.Default.Storefront,
                            label = "Store orders & chats",
                            tint = Color(0xFF2563EB),
                        ) { onNavigate(AppRoutes.storeOrders()) }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
                    }
                    
                    Text(
                        "Personal Account",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Person,
                        label = "Edit personal profile",
                        tint = Color(0xFF10B981),
                    ) { onNavigate(AppRoutes.PROFILE_EDIT) }
                    ProfileMenuRow(
                        icon = Icons.Default.ShoppingBag,
                        label = "My orders & chats",
                        tint = Color(0xFF8B5CF6),
                    ) { onNavigate(AppRoutes.profileOrders()) }
                    ProfileMenuRow(
                        icon = Icons.Default.Favorite,
                        label = "My favorites",
                        tint = Color(0xFFEF4444),
                    ) { onNavigate(AppRoutes.PROFILE_FAVORITES) }
                    ProfileMenuRow(
                        icon = Icons.Default.LocationOn,
                        label = "My addresses",
                        tint = Color(0xFF6366F1),
                    ) { onNavigate(AppRoutes.PROFILE_ADDRESS) }
                    ProfileMenuRow(
                        icon = Icons.Default.CreditCard,
                        label = "Payment methods",
                        tint = Color(0xFFF59E0B),
                    ) { onNavigate(AppRoutes.PROFILE_PAYMENT) }
                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        label = "Notifications",
                        tint = Color(0xFFEC4899),
                    ) { onNavigate(AppRoutes.PROFILE_NOTIFICATIONS) }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))

                    ProfileMenuRow(
                        icon = Icons.Default.Help,
                        label = "Help & Support",
                        tint = Color(0xFF7C3AED),
                    ) { onNavigate(AppRoutes.PROFILE_HELP) }
                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        tint = Color(0xFF4B5563),
                    ) { onNavigate(AppRoutes.PROFILE_SETTINGS) }
                }
            }
        }

        item {
            Surface(
                onClick = { sessionViewModel.signOut() },
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFEF2F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                    Text("Logout", fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text(
                "Version 1.0.0",
                color = Color(0xFF9CA3AF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StoreOwnerHeader(
    store: Store?,
    loadError: String?,
) {
    val gradient = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF4338CA)))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text("My store", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
        Text("Store profile", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        when {
            store == null && loadError == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            store == null -> {
                Text(loadError.orEmpty(), color = Color(0xFFFECACA), style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val logo = store.logo.trim()
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (logo.isNotEmpty()) {
                            AsyncImage(
                                model = logo,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            store.name.ifBlank { "Unnamed store" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (store.description.isNotBlank()) {
                            Text(
                                store.description,
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Text(
                            "ID: ${store.storeId}",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            buildString {
                                append("Rating ")
                                append(String.format(Locale.US, "%.1f", store.rating))
                                if (store.reviewCount > 0) {
                                    append(" (")
                                    append(store.reviewCount)
                                    append(" reviews)")
                                }
                            },
                            color = Color(0xFFFDE68A),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
