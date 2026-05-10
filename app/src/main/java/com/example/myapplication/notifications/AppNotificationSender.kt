package com.example.myapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.data.repository.NotificationTypes
import com.example.myapplication.data.repository.UserNotificationItem
import com.example.myapplication.navigation.AppRoutes

object AppNotificationSender {
    private const val CHANNEL_ID = "phelia_general"
    private const val CHANNEL_NAME = "Notifications"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        )
        mgr.createNotificationChannel(channel)
    }

    fun trayTapRoute(item: UserNotificationItem, userRole: UserRole?): String? =
        when (item.type) {
            NotificationTypes.PRODUCT_QUESTION_ASKED ->
                item.productId.takeIf { it.isNotBlank() && userRole == UserRole.STORE_OWNER }?.let { pid ->
                    AppRoutes.storeProductDetail(pid, item.questionId)
                }
            NotificationTypes.PRODUCT_QUESTION_ANSWERED ->
                item.productId.takeIf { it.isNotBlank() }?.let { AppRoutes.productDetail(it, item.questionId) }
            NotificationTypes.PRICE_DROP ->
                item.productId.takeIf { it.isNotBlank() }?.let { pid ->
                    if (userRole == UserRole.STORE_OWNER) AppRoutes.storeProductDetail(pid)
                    else AppRoutes.productDetail(pid)
                }
            NotificationTypes.NEW_ORDER_FOR_STORE ->
                item.orderId.takeIf { it.isNotBlank() }?.let { AppRoutes.storeOrderDetail(it) }
            NotificationTypes.ORDER_STATUS_UPDATED ->
                item.orderId.takeIf { it.isNotBlank() }?.let { AppRoutes.profileOrderDetail(it) }
            NotificationTypes.NEW_MESSAGE ->
                if (item.storeId.isNotBlank() && item.orderId.isNotBlank()) {
                    AppRoutes.messaging(item.storeId, item.orderId)
                } else {
                    null
                }
            NotificationTypes.SUPPORT_TICKET_SUBMITTED ->
                item.orderId.takeIf { it.isNotBlank() && userRole == UserRole.ADMIN }
                    ?.let { AppRoutes.adminSupportTicketDetail(it) }
            else -> null
        }

    fun showTrayNotification(
        context: Context,
        item: UserNotificationItem,
        userRole: UserRole?,
    ) {
        ensureChannel(context)
        val route = trayTapRoute(item, userRole)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!route.isNullOrBlank()) {
                putExtra(MainActivity.EXTRA_PENDING_NAV_ROUTE, route)
            }
        }
        val reqCode = item.id.hashCode()
        val pending = PendingIntent.getActivity(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(item.title)
            .setContentText(item.body.ifBlank { context.getString(R.string.app_name) })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val mgr = ContextCompat.getSystemService(context, NotificationManager::class.java)!!
        mgr.notify(reqCode, notification)
    }
}
