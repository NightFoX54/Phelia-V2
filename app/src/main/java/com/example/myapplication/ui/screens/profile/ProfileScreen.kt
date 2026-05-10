package com.example.myapplication.ui.screens.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.navigation.AppRoutes
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.OrderHistoryViewModel
import com.example.myapplication.viewmodel.SessionViewModel
import com.example.myapplication.viewmodel.StoreOwnerProfileViewModel

@Composable
fun ProfileScreen(
    sessionViewModel: SessionViewModel,
    orderHistoryViewModel: OrderHistoryViewModel,
    favoritesViewModel: FavoritesViewModel,
    storeOwnerProfileViewModel: StoreOwnerProfileViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val user by sessionViewModel.user.collectAsState()
    val profile = user ?: return

    when (profile.role) {
        UserRole.STORE_OWNER -> {
            StoreOwnerProfileScreen(
                sessionViewModel = sessionViewModel,
                storeOwnerProfileViewModel = storeOwnerProfileViewModel,
                orderHistoryViewModel = orderHistoryViewModel,
                favoritesViewModel = favoritesViewModel,
                onNavigate = onNavigate,
                modifier = modifier,
            )
            return
        }
        else -> { /* customer + admin: existing profile */ }
    }

    val orders by orderHistoryViewModel.orders.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()
    val orderCountText = orders.size.toString()
    val favoritesCountText = favoriteIds.size.toString()
    val isAdmin = profile.role == UserRole.ADMIN

    val scheme = MaterialTheme.colorScheme
    val (badgeBg, badgeFg, badgeText) = when (profile.role) {
        UserRole.ADMIN -> Triple(scheme.primaryContainer, scheme.onPrimaryContainer, "Admin")
        UserRole.STORE_OWNER -> Triple(scheme.secondaryContainer, scheme.onSecondaryContainer, "Store Owner")
        UserRole.CUSTOMER -> Triple(scheme.tertiaryContainer, scheme.onTertiaryContainer, "Customer")
    }

    val gradient = Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Profile", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(profile.email, color = Color.White.copy(alpha = 0.85f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(badgeText, color = badgeFg, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isAdmin) {
                    ProfileQuickCard(
                        icon = Icons.Default.Dashboard,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        value = null,
                        label = "Dashboard",
                        onClick = { onNavigate(AppRoutes.ADMIN_DASHBOARD) },
                        modifier = Modifier.weight(1f),
                    )
                    ProfileQuickCard(
                        icon = Icons.Default.People,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        value = null,
                        label = "Users",
                        onClick = { onNavigate(AppRoutes.USER_MANAGEMENT) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    ProfileQuickCard(
                        icon = Icons.Default.Inventory,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        value = orderCountText,
                        label = "Order History",
                        onClick = { onNavigate(AppRoutes.profileOrders()) },
                        modifier = Modifier.weight(1f),
                    )
                    ProfileQuickCard(
                        icon = Icons.Default.Favorite,
                        iconBg = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Column {
                    if (isAdmin) {
                        ProfileMenuRow(icon = Icons.Default.Person, label = "Edit Profile", tint = MaterialTheme.colorScheme.primary) { onNavigate(AppRoutes.PROFILE_EDIT) }
                        ProfileMenuRow(icon = Icons.Default.People, label = "Manage Users", tint = MaterialTheme.colorScheme.primary) { onNavigate(AppRoutes.USER_MANAGEMENT) }
                        ProfileMenuRow(icon = Icons.Default.Storefront, label = "Manage Stores", tint = MaterialTheme.colorScheme.tertiary) { onNavigate(AppRoutes.STORE_MANAGEMENT) }
                        ProfileMenuRow(icon = Icons.Default.AppRegistration, label = "Store Applications", tint = MaterialTheme.colorScheme.secondary) { onNavigate(AppRoutes.ADMIN_STORE_APPLICATIONS) }
                        ProfileMenuRow(icon = Icons.Default.Inventory, label = "Inactive Products", tint = MaterialTheme.colorScheme.error) { onNavigate(AppRoutes.ADMIN_INACTIVE_PRODUCTS) }
                    } else {
                        ProfileMenuRow(icon = Icons.Default.Person, label = "Edit Profile", tint = MaterialTheme.colorScheme.primary) { onNavigate(AppRoutes.PROFILE_EDIT) }
                        ProfileMenuRow(icon = Icons.Default.LocationOn, label = "Shipping Address", tint = MaterialTheme.colorScheme.primary) { onNavigate(AppRoutes.PROFILE_ADDRESS) }
                        ProfileMenuRow(icon = Icons.Default.CreditCard, label = "Payment Methods", tint = MaterialTheme.colorScheme.tertiary) { onNavigate(AppRoutes.PROFILE_PAYMENT) }
                        ProfileMenuRow(icon = Icons.Default.Notifications, label = "Notifications", tint = MaterialTheme.colorScheme.secondary) { onNavigate(AppRoutes.PROFILE_NOTIFICATIONS) }
                    }
                    ProfileMenuRow(icon = Icons.Default.Help, label = "Help & Support", tint = MaterialTheme.colorScheme.primary) { onNavigate(AppRoutes.PROFILE_HELP) }
                    ProfileMenuRow(icon = Icons.Default.Settings, label = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant) { onNavigate(AppRoutes.PROFILE_SETTINGS) }
                }
            }
        }

        item {
            Surface(
                onClick = { sessionViewModel.signOut() },
                color = MaterialTheme.colorScheme.surface,
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
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                    Text("Logout", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text(
                "Version 1.0.0",
                color = MaterialTheme.colorScheme.outline,
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
internal fun ProfileQuickCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            if (value != null) {
                Text(value, fontWeight = FontWeight.Bold, color = iconTint, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 6.dp))
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
}

