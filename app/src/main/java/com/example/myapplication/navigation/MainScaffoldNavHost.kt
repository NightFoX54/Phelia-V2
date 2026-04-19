package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.ui.components.BottomNavBar
import com.example.myapplication.ui.screens.admin.AdminCatalogScreen
import com.example.myapplication.ui.screens.admin.AdminDashboardScreen
import com.example.myapplication.ui.screens.admin.AdminInactiveProductsScreen
import com.example.myapplication.ui.screens.admin.StoreDetailScreen
import com.example.myapplication.ui.screens.admin.StoreManagementScreen
import com.example.myapplication.ui.screens.admin.StoreApplicationsAdminScreen
import com.example.myapplication.ui.screens.admin.UserManagementScreen
import com.example.myapplication.ui.screens.cart.CartScreen
import com.example.myapplication.ui.screens.checkout.CheckoutScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.order.OrderSuccessScreen
import com.example.myapplication.ui.screens.product.ProductDetailAudience
import com.example.myapplication.ui.screens.product.ProductDetailScreen
import com.example.myapplication.ui.screens.product.ProductListingScreen
import com.example.myapplication.ui.screens.profile.FavoritesScreen
import com.example.myapplication.ui.screens.profile.OrderDetailScreen
import com.example.myapplication.ui.screens.profile.OrderHistoryScreen
import com.example.myapplication.ui.screens.profile.PaymentMethodsScreen
import com.example.myapplication.ui.screens.profile.EditStoreScreen
import com.example.myapplication.ui.screens.profile.ProfileScreen
import com.example.myapplication.ui.screens.profile.ProfileSubPagesScreen
import com.example.myapplication.ui.screens.profile.ShippingAddressesScreen
import com.example.myapplication.ui.screens.store.ProductFormScreen
import com.example.myapplication.ui.screens.store.StoreDashboardScreen
import com.example.myapplication.ui.screens.store.StoreOrderDetailScreen
import com.example.myapplication.ui.screens.store.StoreOrdersScreen
import com.example.myapplication.ui.screens.store.PublicStoreScreen
import com.example.myapplication.ui.screens.store.StoreProductsScreen
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.CheckoutViewModel
import com.example.myapplication.viewmodel.CustomerAccountViewModel
import com.example.myapplication.viewmodel.OrderHistoryViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.ProductEngagementViewModel
import com.example.myapplication.viewmodel.ProductEngagementViewModelFactory
import com.example.myapplication.viewmodel.SessionViewModel
import com.example.myapplication.viewmodel.StoreOrderDetailViewModel
import com.example.myapplication.viewmodel.StoreOrderDetailViewModelFactory
import com.example.myapplication.viewmodel.StoreOrdersViewModel
import com.example.myapplication.viewmodel.StoreOwnerProfileViewModel
import com.example.myapplication.viewmodel.StoreProductsViewModel

@Composable
fun MainScaffoldNavHost(sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val cartViewModel: CartViewModel = viewModel(viewModelStoreOwner = activity)
    val favoritesViewModel: FavoritesViewModel = viewModel(viewModelStoreOwner = activity)
    val customerAccountViewModel: CustomerAccountViewModel = viewModel(viewModelStoreOwner = activity)
    val checkoutViewModel: CheckoutViewModel = viewModel(viewModelStoreOwner = activity)
    val orderHistoryViewModel: OrderHistoryViewModel = viewModel(viewModelStoreOwner = activity)
    val storeOwnerProfileViewModel: StoreOwnerProfileViewModel = viewModel(viewModelStoreOwner = activity)
    val storeProductsViewModel: StoreProductsViewModel = viewModel(viewModelStoreOwner = activity)
    val storeOrdersViewModel: StoreOrdersViewModel = viewModel(viewModelStoreOwner = activity)
    val user by sessionViewModel.user.collectAsState()
    val profile = user
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when (profile.role) {
        UserRole.CUSTOMER -> AppRoutes.HOME
        UserRole.STORE_OWNER -> AppRoutes.STORE_DASHBOARD
        UserRole.ADMIN -> AppRoutes.ADMIN_DASHBOARD
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val hideBottomBarRoutes = setOf(
        AppRoutes.CHECKOUT,
        AppRoutes.PRODUCT_DETAIL,
        AppRoutes.PRODUCT_FORM,
        AppRoutes.STORE_ORDER_DETAIL,
        AppRoutes.STORE_DETAIL,
        AppRoutes.ADMIN_STORE_APPLICATIONS,
        AppRoutes.PUBLIC_STORE,
        AppRoutes.PROFILE_EDIT,
        AppRoutes.PROFILE_ADDRESS,
        AppRoutes.PROFILE_PAYMENT,
        AppRoutes.PROFILE_NOTIFICATIONS,
        AppRoutes.PROFILE_HELP,
        AppRoutes.PROFILE_SETTINGS,
        AppRoutes.PROFILE_ORDERS,
        AppRoutes.PROFILE_FAVORITES,
        AppRoutes.STORE_PROFILE_EDIT,
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != null && !hideBottomBarRoutes.contains(currentRoute) &&
                !currentRoute.startsWith("order_success/") &&
                !currentRoute.startsWith("profile/orders/") &&
                !currentRoute.startsWith("store-product/") &&
                !currentRoute.startsWith("public-store/")
            ) {
                BottomNavBar(navController = navController, role = profile.role)
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
                    onOpenNotifications = { navController.navigate(AppRoutes.PROFILE_NOTIFICATIONS) },
                    onOpenCart = { navController.navigate(AppRoutes.CART) },
                    onOpenProducts = { navController.navigate(AppRoutes.PRODUCTS) },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                    onOpenStore = { storeId: String -> navController.navigate(AppRoutes.publicStore(storeId)) },
                    favoritesViewModel = favoritesViewModel,
                )
            }
            composable(AppRoutes.PRODUCTS) {
                ProductListingScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                    onOpenStore = { storeId: String -> navController.navigate(AppRoutes.publicStore(storeId)) },
                    favoritesViewModel = favoritesViewModel,
                )
            }
            composable(
                route = AppRoutes.PRODUCT_DETAIL,
                arguments = AppRoutes.productDetailArgs,
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId").orEmpty()
                val engagementViewModel: ProductEngagementViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "engagement_$productId",
                    factory = ProductEngagementViewModelFactory(productId),
                )
                ProductDetailScreen(
                    productId = productId,
                    cartViewModel = cartViewModel,
                    favoritesViewModel = favoritesViewModel,
                    engagementViewModel = engagementViewModel,
                    onBack = { navController.popBackStack() },
                    onAddedToCart = { navController.navigate(AppRoutes.CART) },
                    onOpenStore = { storeId -> navController.navigate(AppRoutes.publicStore(storeId)) },
                )
            }
            composable(
                route = AppRoutes.PUBLIC_STORE,
                arguments = AppRoutes.publicStoreArgs,
            ) { backStackEntry ->
                val sid = backStackEntry.arguments?.getString("storeId").orEmpty()
                PublicStoreScreen(
                    storeId = sid,
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                    onOpenStore = { storeId: String -> navController.navigate(AppRoutes.publicStore(storeId)) },
                    favoritesViewModel = favoritesViewModel,
                )
            }
            composable(AppRoutes.CART) {
                CartScreen(
                    cartViewModel = cartViewModel,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(AppRoutes.CHECKOUT) },
                )
            }
            composable(AppRoutes.CHECKOUT) {
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    accountViewModel = customerAccountViewModel,
                    checkoutViewModel = checkoutViewModel,
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(AppRoutes.orderSuccess(orderId)) {
                            popUpTo(AppRoutes.CART) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onAddShippingAddress = { navController.navigate(AppRoutes.PROFILE_ADDRESS) },
                    onAddPaymentMethod = { navController.navigate(AppRoutes.PROFILE_PAYMENT) },
                )
            }
            composable(
                route = AppRoutes.ORDER_SUCCESS,
                arguments = AppRoutes.orderSuccessArgs,
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
                OrderSuccessScreen(
                    orderId = orderId,
                    onContinueShopping = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onTrackOrder = { navController.navigate(AppRoutes.PROFILE) },
                )
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    sessionViewModel = sessionViewModel,
                    orderHistoryViewModel = orderHistoryViewModel,
                    favoritesViewModel = favoritesViewModel,
                    storeOwnerProfileViewModel = storeOwnerProfileViewModel,
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
            composable(AppRoutes.STORE_PROFILE_EDIT) {
                EditStoreScreen(
                    viewModel = storeOwnerProfileViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppRoutes.PROFILE_EDIT) { ProfileSubPagesScreen("Edit Profile", onBack = { navController.popBackStack() }) }
            composable(AppRoutes.PROFILE_ADDRESS) {
                ShippingAddressesScreen(
                    accountViewModel = customerAccountViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppRoutes.PROFILE_PAYMENT) {
                PaymentMethodsScreen(
                    accountViewModel = customerAccountViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppRoutes.PROFILE_NOTIFICATIONS) {
                ProfileSubPagesScreen(
                    "Notifications",
                    onBack = { navController.popBackStack() },
                    onNavigateToRoute = { route -> navController.navigate(route) },
                )
            }
            composable(AppRoutes.PROFILE_HELP) { ProfileSubPagesScreen("Help & Support", onBack = { navController.popBackStack() }) }
            composable(AppRoutes.PROFILE_SETTINGS) { ProfileSubPagesScreen("Settings", onBack = { navController.popBackStack() }) }
            composable(
                route = AppRoutes.PROFILE_ORDER_DETAIL,
                arguments = AppRoutes.profileOrderDetailArgs,
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
                OrderDetailScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { productId ->
                        navController.navigate(AppRoutes.productDetail(productId))
                    },
                )
            }
            composable(AppRoutes.PROFILE_ORDERS) {
                OrderHistoryScreen(
                    viewModel = orderHistoryViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenOrderDetail = { id -> navController.navigate(AppRoutes.profileOrderDetail(id)) },
                )
            }
            composable(AppRoutes.PROFILE_FAVORITES) {
                FavoritesScreen(
                    favoritesViewModel = favoritesViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                )
            }

            composable(AppRoutes.STORE_DASHBOARD) {
                StoreDashboardScreen(
                    storeProductsViewModel = storeProductsViewModel,
                    onAddProduct = { navController.navigate(AppRoutes.PRODUCT_FORM) },
                    onOpenProductDetail = { navController.navigate(AppRoutes.storeProductDetail(it)) },
                    onEditProduct = { navController.navigate(AppRoutes.productFormEdit(it)) },
                )
            }
            composable(AppRoutes.STORE_PRODUCTS) {
                StoreProductsScreen(
                    viewModel = storeProductsViewModel,
                    onAddProduct = { navController.navigate(AppRoutes.PRODUCT_FORM) },
                    onEditProduct = { navController.navigate(AppRoutes.productFormEdit(it)) },
                    onOpenProductDetail = { id -> navController.navigate(AppRoutes.storeProductDetail(id)) },
                )
            }
            composable(
                route = AppRoutes.STORE_PRODUCT_DETAIL,
                arguments = AppRoutes.storeProductDetailArgs,
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId").orEmpty()
                val store by storeOwnerProfileViewModel.store.collectAsState()
                val engagementVm: ProductEngagementViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "eng_store_$productId",
                    factory = ProductEngagementViewModelFactory(productId),
                )
                ProductDetailScreen(
                    productId = productId,
                    audience = ProductDetailAudience.StoreOwner,
                    cartViewModel = cartViewModel,
                    favoritesViewModel = favoritesViewModel,
                    engagementViewModel = engagementVm,
                    ownerStoreId = store?.storeId.orEmpty(),
                    onBack = { navController.popBackStack() },
                    onEditProduct = { navController.navigate(AppRoutes.productFormEdit(productId)) },
                )
            }
            composable(AppRoutes.STORE_ORDERS) {
                StoreOrdersScreen(
                    viewModel = storeOrdersViewModel,
                    onOpenOrder = { navController.navigate(AppRoutes.storeOrderDetail(it)) },
                )
            }
            composable(
                route = AppRoutes.STORE_ORDER_DETAIL,
                arguments = AppRoutes.storeOrderDetailArgs,
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
                val detailVm: StoreOrderDetailViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "store_order_detail_$orderId",
                    factory = StoreOrderDetailViewModelFactory(orderId) {
                        storeOrdersViewModel.refreshOrdersIfPossible()
                    },
                )
                StoreOrderDetailScreen(
                    viewModel = detailVm,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoutes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    onManageUsers = { navController.navigate(AppRoutes.USER_MANAGEMENT) },
                    onManageStores = { navController.navigate(AppRoutes.STORE_MANAGEMENT) },
                    onInactiveProducts = { navController.navigate(AppRoutes.ADMIN_INACTIVE_PRODUCTS) },
                    onStoreApplications = { navController.navigate(AppRoutes.ADMIN_STORE_APPLICATIONS) },
                    onCatalogMeta = { navController.navigate(AppRoutes.ADMIN_CATALOG) },
                )
            }
            composable(AppRoutes.ADMIN_CATALOG) {
                AdminCatalogScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.ADMIN_STORE_APPLICATIONS) {
                StoreApplicationsAdminScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.ADMIN_INACTIVE_PRODUCTS) {
                AdminInactiveProductsScreen(onBack = { navController.popBackStack() })
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
