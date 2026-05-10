package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.navigation.AppRoutes

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private fun routeMatchesTab(currentRoute: String?, tabRoute: String): Boolean {
    if (currentRoute == null) return false
    return when (tabRoute) {
        AppRoutes.STORE_ORDERS -> currentRoute.startsWith("store-orders")
        AppRoutes.PRODUCTS -> currentRoute.startsWith("products")
        else -> currentRoute == tabRoute
    }
}

@Composable
fun BottomNavBar(
    navController: NavController,
    role: UserRole,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = when (role) {
        UserRole.CUSTOMER -> listOf(
            BottomNavItem(AppRoutes.HOME, "Home", Icons.Default.Home),
            BottomNavItem(AppRoutes.CART, "Cart", Icons.Default.ShoppingCart),
            BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Default.AccountCircle),
        )
        UserRole.STORE_OWNER -> listOf(
            BottomNavItem(AppRoutes.HOME, "Shop", Icons.Default.Home),
            BottomNavItem(AppRoutes.STORE_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
            BottomNavItem(AppRoutes.STORE_ORDERS, "Orders", Icons.Default.ShoppingCart),
            BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Default.AccountCircle),
        )
        UserRole.ADMIN -> listOf(
            BottomNavItem(AppRoutes.ADMIN_DASHBOARD, "Dashboard", Icons.Default.InsertChart),
            BottomNavItem(AppRoutes.USER_MANAGEMENT, "Users", Icons.Default.People),
            BottomNavItem(AppRoutes.STORE_MANAGEMENT, "Stores", Icons.Default.Storefront),
            BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Default.AccountCircle),
        )
    }

    val scheme = MaterialTheme.colorScheme
    NavigationBar(containerColor = scheme.surface) {
        items.forEach { item ->
            val selected = routeMatchesTab(currentRoute, item.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (routeMatchesTab(currentRoute, item.route)) return@NavigationBarItem
                    if (item.route == AppRoutes.PROFILE) {
                        val popped = navController.popBackStack(AppRoutes.PROFILE, inclusive = false, saveState = false)
                        if (!popped) {
                            navController.navigate(AppRoutes.PROFILE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        return@NavigationBarItem
                    }
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = scheme.primary,
                    selectedTextColor = scheme.primary,
                    indicatorColor = scheme.primary.copy(alpha = 0.14f),
                    unselectedIconColor = scheme.onSurfaceVariant,
                    unselectedTextColor = scheme.onSurfaceVariant,
                ),
            )
        }
    }
}

