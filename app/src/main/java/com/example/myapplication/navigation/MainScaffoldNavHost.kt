package com.example.myapplication.navigation

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.notifications.NotificationTraySideEffect
import com.example.myapplication.viewmodel.UserSettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.ui.components.BottomNavBar
import com.google.firebase.Timestamp
import com.example.myapplication.ui.screens.admin.AdminCatalogScreen
import com.example.myapplication.ui.screens.admin.AdminDashboardScreen
import com.example.myapplication.ui.screens.admin.AdminInactiveProductsScreen
import com.example.myapplication.ui.screens.admin.AdminSupportTicketDetailScreen
import com.example.myapplication.ui.screens.admin.AdminSupportTicketsScreen
import com.example.myapplication.ui.screens.admin.StoreDetailScreen
import com.example.myapplication.ui.screens.admin.StoreManagementScreen
import com.example.myapplication.ui.screens.admin.StoreUpdateRequestsScreen
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
import com.example.myapplication.ui.screens.profile.StoreApplicationRetryScreen
import com.example.myapplication.ui.screens.store.ProductFormScreen
import com.example.myapplication.ui.screens.store.StoreDashboardScreen
import com.example.myapplication.ui.screens.store.StoreOrderDetailScreen
import com.example.myapplication.ui.screens.store.StoreOrdersScreen
import com.example.myapplication.ui.screens.store.PublicStoreScreen
import com.example.myapplication.ui.screens.messaging.MessagingScreen
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
    val userSettingsViewModel: UserSettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application),
    )
    val user by sessionViewModel.user.collectAsState()
    val profile = user
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional: handle denial */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        userSettingsViewModel.loadSettings()
        userSettingsViewModel.loadNotifications()
    }

    val trayNotifications by userSettingsViewModel.notifications.collectAsState()
    NotificationTraySideEffect(notifications = trayNotifications, userRole = profile.role)

    LaunchedEffect(navController) {
        NavigationIntentStore.pendingRoute.collectLatest { route ->
            if (!route.isNullOrBlank()) {
                delay(50)
                runCatching {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
                NavigationIntentStore.clear()
            }
        }
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
    val hideBottomBar = currentRoute != null && (
        hideBottomBarRoutes.contains(currentRoute) ||
            currentRoute.startsWith("product/") ||
            currentRoute.startsWith("store-product/") ||
            currentRoute.startsWith("admin/support-tickets")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (currentRoute != null && !hideBottomBar &&
                !currentRoute.startsWith("order_success/") &&
                !currentRoute.startsWith("profile/orders/") &&
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
                val notifications by userSettingsViewModel.notifications.collectAsState()
                val customerChats by orderHistoryViewModel.chats.collectAsState()

                val uid = orderHistoryViewModel.getCurrentUserId()
                val unreadThreads = if (uid.isBlank()) 0 else customerChats.count { chat ->
                    val lastRead: Timestamp? = chat.lastReadBy[uid]
                    lastRead == null || lastRead.seconds < chat.lastMessageTimestamp.seconds
                }
                HomeScreen(
                    userName = profile.name,
                    unreadNotificationsCount = notifications.count { !it.isRead },
                    unreadMessagesCount = unreadThreads,
                    onOpenNotifications = { navController.navigate(AppRoutes.PROFILE_NOTIFICATIONS) },
                    onOpenMessages = { navController.navigate(AppRoutes.profileOrders(tab = 1)) },
                    onOpenCart = { navController.navigate(AppRoutes.CART) },
                    onOpenProducts = { navController.navigate(AppRoutes.products(saleOnly = false)) },
                    onOpenSaleProducts = { navController.navigate(AppRoutes.products(saleOnly = true)) },
                    onOpenProduct = { navController.navigate(AppRoutes.productDetail(it)) },
                    onOpenStore = { storeId: String -> navController.navigate(AppRoutes.publicStore(storeId)) },
                    favoritesViewModel = favoritesViewModel,
                )
            }
            composable(
                route = AppRoutes.PRODUCTS,
                arguments = AppRoutes.productsArgs,
            ) { backStackEntry ->
                val saleOnly = backStackEntry.arguments?.getBoolean("saleOnly") ?: false
                ProductListingScreen(
                    showOnlyDiscounted = saleOnly,
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
                val highlightQuestionId = AppRoutes.decodeQuestionIdRouteArg(backStackEntry.arguments?.getString("questionId"))
                val engagementViewModel: ProductEngagementViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "engagement_$productId",
                    factory = ProductEngagementViewModelFactory(productId),
                )
                ProductDetailScreen(
                    productId = productId,
                    highlightQuestionId = highlightQuestionId,
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
            composable(AppRoutes.PROFILE_EDIT) { 
                ProfileSubPagesScreen(
                    "Edit Profile", 
                    onBack = { navController.popBackStack() },
                    sessionViewModel = sessionViewModel
                ) 
            }
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
                    sessionViewModel = sessionViewModel,
                )
            }
            composable(AppRoutes.PROFILE_HELP) { ProfileSubPagesScreen("Help & Support", onBack = { navController.popBackStack() }) }
            composable(AppRoutes.PROFILE_SETTINGS) { ProfileSubPagesScreen("Settings", onBack = { navController.popBackStack() }) }
            composable(AppRoutes.STORE_APPLICATION_RETRY) {
                StoreApplicationRetryScreen(onBack = { navController.popBackStack() })
            }
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
                    onMessageStore = { storeId, suborderId ->
                        navController.navigate(AppRoutes.messaging(storeId, suborderId))
                    }
                )
            }
            composable(
                route = AppRoutes.MESSAGING,
                arguments = AppRoutes.messagingArgs,
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
                val suborderId = backStackEntry.arguments?.getString("suborderId").orEmpty()
                MessagingScreen(
                    storeId = storeId,
                    suborderId = suborderId,
                    onBack = { navController.popBackStack() },
                    onOpenParentOrder = { orderId ->
                        when (profile.role) {
                            UserRole.STORE_OWNER -> navController.navigate(AppRoutes.storeOrderDetail(orderId))
                            else -> navController.navigate(AppRoutes.profileOrderDetail(orderId))
                        }
                    },
                )
            }
            composable(
                route = AppRoutes.PROFILE_ORDERS,
                arguments = listOf(
                    androidx.navigation.navArgument("tab") {
                        type = androidx.navigation.NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                OrderHistoryScreen(
                    viewModel = orderHistoryViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenOrderDetail = { id -> navController.navigate(AppRoutes.profileOrderDetail(id)) },
                    onOpenChat = { storeId, suborderId ->
                        navController.navigate(AppRoutes.messaging(storeId, suborderId))
                    },
                    onOpenParentOrderFromMessages = { navController.navigate(AppRoutes.profileOrderDetail(it)) },
                    initialTab = initialTab,
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
                    userSettingsViewModel = userSettingsViewModel,
                    onAddProduct = { navController.navigate(AppRoutes.PRODUCT_FORM) },
                    onOpenProductDetail = { navController.navigate(AppRoutes.storeProductDetail(it)) },
                    onEditProduct = { navController.navigate(AppRoutes.productFormEdit(it)) },
                    onNavigateToRetry = { navController.navigate(AppRoutes.STORE_APPLICATION_RETRY) },
                    onOpenNotifications = { navController.navigate(AppRoutes.PROFILE_NOTIFICATIONS) },
                    onNavigateToStoreProducts = { navController.navigate(AppRoutes.STORE_PRODUCTS) },
                    onNavigateToStoreOrders = { navController.navigate(AppRoutes.storeOrders(tab = 0)) },
                    onBack = null,
                )
            }
            composable(AppRoutes.STORE_PRODUCTS) {
                StoreProductsScreen(
                    viewModel = storeProductsViewModel,
                    onBack = { navController.popBackStack() },
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
                val highlightQuestionId = AppRoutes.decodeQuestionIdRouteArg(backStackEntry.arguments?.getString("questionId"))
                val store by storeOwnerProfileViewModel.store.collectAsState()
                val engagementVm: ProductEngagementViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "eng_store_$productId",
                    factory = ProductEngagementViewModelFactory(productId),
                )
                ProductDetailScreen(
                    productId = productId,
                    highlightQuestionId = highlightQuestionId,
                    audience = ProductDetailAudience.StoreOwner,
                    cartViewModel = cartViewModel,
                    favoritesViewModel = favoritesViewModel,
                    engagementViewModel = engagementVm,
                    ownerStoreId = store?.storeId.orEmpty(),
                    onBack = { navController.popBackStack() },
                    onEditProduct = { navController.navigate(AppRoutes.productFormEdit(productId)) },
                )
            }
            composable(
                route = AppRoutes.STORE_ORDERS,
                arguments = listOf(
                    androidx.navigation.navArgument("tab") {
                        type = androidx.navigation.NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                StoreOrdersScreen(
                    viewModel = storeOrdersViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenOrder = { navController.navigate(AppRoutes.storeOrderDetail(it)) },
                    onOpenChat = { storeId, suborderId ->
                        navController.navigate(AppRoutes.messaging(storeId, suborderId))
                    },
                    onOpenParentOrderFromMessages = { navController.navigate(AppRoutes.storeOrderDetail(it)) },
                    initialTab = initialTab
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
                    onMessageBuyer = { storeId, suborderId ->
                        navController.navigate(AppRoutes.messaging(storeId, suborderId))
                    }
                )
            }

            composable(AppRoutes.ADMIN_DASHBOARD) {
                LaunchedEffect(Unit) {
                    userSettingsViewModel.loadNotifications()
                }
                AdminDashboardScreen(
                    onManageUsers = { navController.navigate(AppRoutes.USER_MANAGEMENT) },
                    onManageStores = { navController.navigate(AppRoutes.STORE_MANAGEMENT) },
                    onStoreUpdateRequests = { navController.navigate(AppRoutes.ADMIN_STORE_UPDATE_REQUESTS) },
                    onInactiveProducts = { navController.navigate(AppRoutes.ADMIN_INACTIVE_PRODUCTS) },
                    onStoreApplications = { navController.navigate(AppRoutes.ADMIN_STORE_APPLICATIONS) },
                    onCatalogMeta = { navController.navigate(AppRoutes.ADMIN_CATALOG) },
                    onSupportTickets = { navController.navigate(AppRoutes.ADMIN_SUPPORT_TICKETS) },
                    onOpenProduct = { productId -> navController.navigate(AppRoutes.productDetail(productId)) },
                    onOpenStore = { storeId -> navController.navigate(AppRoutes.storeDetail(storeId)) },
                )
            }
            composable(AppRoutes.ADMIN_SUPPORT_TICKETS) {
                AdminSupportTicketsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTicket = { id -> navController.navigate(AppRoutes.adminSupportTicketDetail(id)) },
                )
            }
            composable(
                route = AppRoutes.ADMIN_SUPPORT_TICKET_DETAIL,
                arguments = AppRoutes.adminSupportTicketDetailArgs,
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId").orEmpty()
                AdminSupportTicketDetailScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() },
                    userSettingsViewModel = userSettingsViewModel,
                )
            }
            composable(AppRoutes.ADMIN_STORE_UPDATE_REQUESTS) {
                StoreUpdateRequestsScreen(
                    onBack = { navController.popBackStack() },
                    userSettingsViewModel = userSettingsViewModel,
                )
            }
            composable(AppRoutes.ADMIN_CATALOG) {
                AdminCatalogScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.ADMIN_STORE_APPLICATIONS) {
                StoreApplicationsAdminScreen(
                    onBack = { navController.popBackStack() },
                    userSettingsViewModel = userSettingsViewModel,
                )
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
