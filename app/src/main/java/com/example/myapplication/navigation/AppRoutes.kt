package com.example.myapplication.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object AppRoutes {
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_SUCCESS = "order_success"
    const val LOGIN = "login"
    const val REGISTER = "register"

    /** @deprecated Use [LOGIN] */
    const val AUTH = LOGIN
    const val PROFILE = "profile"

    // Customer profile subpages
    const val PROFILE_EDIT = "profile/edit"
    const val PROFILE_ADDRESS = "profile/address"
    const val PROFILE_PAYMENT = "profile/payment"
    const val PROFILE_NOTIFICATIONS = "profile/notifications"
    const val PROFILE_HELP = "profile/help"
    const val PROFILE_SETTINGS = "profile/settings"
    const val PROFILE_ORDERS = "profile/orders"
    const val PROFILE_FAVORITES = "profile/favorites"

    // Store owner
    const val STORE_DASHBOARD = "store-dashboard"
    const val STORE_PRODUCTS = "store-products"
    const val STORE_ORDERS = "store-orders"
    const val STORE_ORDER_DETAIL = "store-order/{orderId}"

    // Admin
    const val ADMIN_DASHBOARD = "admin-dashboard"
    const val USER_MANAGEMENT = "user-management"
    const val STORE_MANAGEMENT = "store-management"
    const val STORE_DETAIL = "store/{storeId}"

    // Forms
    const val PRODUCT_FORM = "product-form"
    const val PRODUCT_FORM_EDIT = "product-form/{productId}"

    fun productDetail(productId: String) = "product/$productId"
    fun productFormEdit(productId: String) = "product-form/$productId"
    fun storeOrderDetail(orderId: String) = "store-order/$orderId"
    fun storeDetail(storeId: String) = "store/$storeId"

    val productDetailArgs = listOf(
        navArgument("productId") { type = NavType.StringType },
    )
    val storeOrderDetailArgs = listOf(
        navArgument("orderId") { type = NavType.StringType },
    )
    val storeDetailArgs = listOf(
        navArgument("storeId") { type = NavType.StringType },
    )
    val productFormArgs = listOf(
        navArgument("productId") { type = NavType.StringType },
    )
}

