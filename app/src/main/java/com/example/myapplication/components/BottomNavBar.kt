package com.example.myapplication.components

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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.models.UserRole
import com.example.myapplication.navigation.AppRoutes

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

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
            BottomNavItem(AppRoutes.STORE_DASHBOARD, "Dashboard", Icons.Default.Dashboard),
            BottomNavItem(AppRoutes.STORE_PRODUCTS, "Products", Icons.Default.Inventory),
            BottomNavItem(AppRoutes.STORE_ORDERS, "Orders", Icons.Default.ShoppingCart),
            BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Default.AccountCircle),
        )
        UserRole.ADMIN -> listOf(
            BottomNavItem(AppRoutes.ADMIN_DASHBOARD, "Dashboard", Icons.Default.InsertChart),
            BottomNavItem(AppRoutes.USER_MANAGEMENT, "Users", Icons.Default.People),
            BottomNavItem(AppRoutes.STORE_MANAGEMENT, "Stores", Icons.Default.Storefront),
            BottomNavItem(AppRoutes.PROFILE, "Analytics", Icons.Default.AccountCircle),
        )
    }

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

