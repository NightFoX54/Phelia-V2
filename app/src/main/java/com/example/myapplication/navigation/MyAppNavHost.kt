package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.example.myapplication.components.BottomNavBar
import com.example.myapplication.models.UserRole
import com.example.myapplication.screens.admin.AdminDashboardScreen
import com.example.myapplication.screens.admin.StoreManagementScreen
import com.example.myapplication.screens.admin.StoreDetailScreen
import com.example.myapplication.screens.admin.UserManagementScreen
import com.example.myapplication.screens.auth.AuthScreen
import com.example.myapplication.screens.cart.CartScreen
import com.example.myapplication.screens.checkout.CheckoutScreen
import com.example.myapplication.screens.home.HomeScreen
import com.example.myapplication.screens.order.OrderSuccessScreen
import com.example.myapplication.screens.product.ProductDetailScreen
import com.example.myapplication.screens.product.ProductListingScreen
import com.example.myapplication.screens.profile.ProfileScreen
import com.example.myapplication.screens.profile.ProfileSubPagesScreen
import com.example.myapplication.screens.store.StoreDashboardScreen
import com.example.myapplication.screens.store.StoreOrderDetailScreen
import com.example.myapplication.screens.store.StoreOrdersScreen
import com.example.myapplication.screens.store.StoreProductsScreen
import com.example.myapplication.screens.store.ProductFormScreen
import com.example.myapplication.viewmodels.SessionViewModel

@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()
    val sessionVm: SessionViewModel = viewModel()
    val user by sessionVm.user.collectAsState()

    val startDestination = when (user.role) {
        UserRole.CUSTOMER -> AppRoutes.HOME
        UserRole.STORE_OWNER -> AppRoutes.STORE_DASHBOARD
        UserRole.ADMIN -> AppRoutes.ADMIN_DASHBOARD
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val hideBottomBarRoutes = setOf(
        AppRoutes.AUTH,
        AppRoutes.CHECKOUT,
        AppRoutes.ORDER_SUCCESS,
        AppRoutes.PRODUCT_DETAIL,
        AppRoutes.PRODUCT_FORM,
        AppRoutes.STORE_ORDER_DETAIL,
        AppRoutes.STORE_DETAIL,
        AppRoutes.PROFILE_EDIT,
        AppRoutes.PROFILE_ADDRESS,
        AppRoutes.PROFILE_PAYMENT,
        AppRoutes.PROFILE_NOTIFICATIONS,
        AppRoutes.PROFILE_HELP,
        AppRoutes.PROFILE_SETTINGS,
        AppRoutes.PROFILE_ORDERS,
        AppRoutes.PROFILE_FAVORITES,
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != null && !hideBottomBarRoutes.contains(currentRoute)) {
                BottomNavBar(navController = navController, role = user.role)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(
                    onOpenCart = { navController.navigate(AppRoutes.CART) },
                    onOpenProducts = { navController.navigate(AppRoutes.PRODUCTS) },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                )
            }
            composable(AppRoutes.PRODUCTS) {
                ProductListingScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                )
            }
            composable(
                route = AppRoutes.PRODUCT_DETAIL,
                arguments = AppRoutes.productDetailArgs,
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId").orEmpty()
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { navController.navigate(AppRoutes.CART) },
                )
            }
            composable(AppRoutes.CART) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(AppRoutes.CHECKOUT) },
                )
            }
            composable(AppRoutes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onConfirm = { navController.navigate(AppRoutes.ORDER_SUCCESS) },
                )
            }
            composable(AppRoutes.ORDER_SUCCESS) {
                OrderSuccessScreen(
                    onContinueShopping = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onTrackOrder = { navController.navigate(AppRoutes.PROFILE) },
                )
            }
            composable(AppRoutes.AUTH) {
                AuthScreen(
                    onDone = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    sessionViewModel = sessionVm,
                )
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    sessionViewModel = sessionVm,
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
        composable(AppRoutes.PROFILE_EDIT) { ProfileSubPagesScreen("Edit Profile", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_ADDRESS) { ProfileSubPagesScreen("Shipping Address", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_PAYMENT) { ProfileSubPagesScreen("Payment Methods", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_NOTIFICATIONS) { ProfileSubPagesScreen("Notifications", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_HELP) { ProfileSubPagesScreen("Help & Support", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_SETTINGS) { ProfileSubPagesScreen("Settings", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_ORDERS) { ProfileSubPagesScreen("Order History", onBack = { navController.popBackStack() }) }
        composable(AppRoutes.PROFILE_FAVORITES) { ProfileSubPagesScreen("Favorites", onBack = { navController.popBackStack() }) }

        // Store Owner
        composable(AppRoutes.STORE_DASHBOARD) {
            StoreDashboardScreen(
                onAddProduct = { navController.navigate(AppRoutes.PRODUCT_FORM) },
                onEditProduct = { navController.navigate(AppRoutes.productFormEdit(it)) },
            )
        }
        composable(AppRoutes.STORE_PRODUCTS) {
            StoreProductsScreen(
                onAddProduct = { navController.navigate(AppRoutes.PRODUCT_FORM) },
                onEditProduct = { navController.navigate(AppRoutes.productFormEdit(it)) },
            )
        }
        composable(AppRoutes.STORE_ORDERS) {
            StoreOrdersScreen(
                onOpenOrder = { navController.navigate(AppRoutes.storeOrderDetail(it)) },
            )
        }
        composable(
            route = AppRoutes.STORE_ORDER_DETAIL,
            arguments = AppRoutes.storeOrderDetailArgs,
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
            StoreOrderDetailScreen(orderId = orderId, onBack = { navController.popBackStack() })
        }

        // Admin
        composable(AppRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onManageUsers = { navController.navigate(AppRoutes.USER_MANAGEMENT) },
                onManageStores = { navController.navigate(AppRoutes.STORE_MANAGEMENT) },
            )
        }
        composable(AppRoutes.USER_MANAGEMENT) { UserManagementScreen(onBack = { navController.popBackStack() }) }
        composable(AppRoutes.STORE_MANAGEMENT) {
            StoreManagementScreen(
                onBack = { navController.popBackStack() },
                onOpenStore = { navController.navigate(AppRoutes.storeDetail(it)) },
            )
        }
        composable(
            route = AppRoutes.STORE_DETAIL,
            arguments = AppRoutes.storeDetailArgs,
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
            StoreDetailScreen(storeId = storeId, onBack = { navController.popBackStack() })
        }

        // Forms
        composable(AppRoutes.PRODUCT_FORM) {
            ProductFormScreen(
                productId = null,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoutes.PRODUCT_FORM_EDIT,
            arguments = AppRoutes.productFormArgs,
        ) { backStackEntry ->
            ProductFormScreen(
                productId = backStackEntry.arguments?.getString("productId"),
                onBack = { navController.popBackStack() },
            )
        }
        }
    }
}

