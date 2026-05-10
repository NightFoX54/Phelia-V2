package com.example.myapplication.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.data.repository.UserNotificationItem

/** Mirrors new Firestore notification docs into the OS notification shade (real-time while the app runs). */
@Composable
fun NotificationTraySideEffect(
    notifications: List<UserNotificationItem>,
    userRole: UserRole?,
) {
    val context = LocalContext.current.applicationContext
    var baselineIds by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(notifications, userRole) {
        if (baselineIds == null) {
            if (notifications.isNotEmpty()) {
                baselineIds = notifications.map { it.id }.toSet()
            }
            return@LaunchedEffect
        }
        val prev = baselineIds!!
        val fresh = notifications.filter { it.id !in prev }
        baselineIds = notifications.map { it.id }.toSet()
        fresh.forEach {
            AppNotificationSender.showTrayNotification(context, it, userRole)
        }
    }
}
