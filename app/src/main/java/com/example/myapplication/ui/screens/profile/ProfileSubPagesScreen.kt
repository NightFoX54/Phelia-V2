package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.data.repository.UserSettings
import com.example.myapplication.data.repository.NotificationTypes
import com.example.myapplication.navigation.AppRoutes
import com.example.myapplication.viewmodel.UserSettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileSubPagesScreen(
    title: String,
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val settingsVm: UserSettingsViewModel = viewModel()
    val userSettings by settingsVm.settings.collectAsState()
    val userProfile by settingsVm.userProfile.collectAsState()
    val notifications by settingsVm.notifications.collectAsState()
    val loading by settingsVm.loading.collectAsState()
    val message by settingsVm.message.collectAsState()

    LaunchedEffect(title) {
        when (title) {
            "Settings" -> settingsVm.loadSettings()
            "Notifications" -> settingsVm.loadNotifications()
            "Edit Profile" -> settingsVm.loadUserProfile()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+90 5XX XXX XX XX") }
    var bio by remember { mutableStateOf("Tech enthusiast and deal hunter.") }
    var emailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            email = it.email
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            AppTopBar(title = title, onBack = onBack, containerColor = Color.White)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (title) {
                "Edit Profile" -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(Color(0xFFE0E7FF), CircleShape),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Profile Photo", fontWeight = FontWeight.SemiBold)
                                    Text("Upload, remove or update avatar", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("Change Photo") }
                                }
                            }
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = emailError != null,
                                supportingText = {
                                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                }
                            )
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            Button(
                                onClick = {
                                    settingsVm.updateProfile(
                                        name = name,
                                        email = email,
                                        onEmailTaken = {
                                            emailError = "Please choose another email address."
                                        },
                                        onSuccess = {
                                            // Optional: Handle success (e.g., show a toast or navigate back)
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !loading
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                    SecurityCard()
                }
                "Notifications" -> {
                    if (loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) { CircularProgressIndicator() }
                    }
                    if (notifications.isEmpty() && !loading) {
                        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("No notifications yet.", color = Color(0xFF6B7280))
                            }
                        }
                    }
                    notifications.forEach { n ->
                        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        settingsVm.markNotificationRead(n.id)
                                        val route = when (n.type) {
                                            NotificationTypes.NEW_ORDER_FOR_STORE ->
                                                n.orderId.takeIf { it.isNotBlank() }?.let { AppRoutes.storeOrderDetail(it) }
                                            NotificationTypes.ORDER_STATUS_UPDATED ->
                                                n.orderId.takeIf { it.isNotBlank() }?.let { AppRoutes.profileOrderDetail(it) }
                                            NotificationTypes.PRICE_DROP ->
                                                n.productId.takeIf { it.isNotBlank() }?.let { AppRoutes.productDetail(it) }
                                            NotificationTypes.STORE_APPLICATION_SUBMITTED ->
                                                AppRoutes.ADMIN_STORE_APPLICATIONS
                                            else -> null
                                        }
                                        if (route != null) onNavigateToRoute(route)
                                    }
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(n.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    if (!n.isRead) {
                                        BadgeLike("New")
                                    }
                                    IconButton(onClick = { settingsVm.deleteNotification(n.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete notification", tint = Color(0xFF9CA3AF))
                                    }
                                }
                                if (n.body.isNotBlank()) {
                                    Text(n.body, color = Color(0xFF4B5563))
                                }
                                Text(formatNotificationDate(n.createdAtMs), color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                "Help & Support" -> {
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("How can we help?", fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Search help articles") }, modifier = Modifier.fillMaxWidth())
                            Text("Popular topics", color = Color(0xFF6B7280))
                            listOf("Order tracking", "Return & refund", "Payment failed", "Account security").forEach { topic ->
                                ChipButton(topic)
                            }
                        }
                    }
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Contact support", fontWeight = FontWeight.Bold)
                            Text("Live chat: 09:00 - 23:00", color = Color(0xFF4B5563))
                            Text("support@myapplication.com", color = Color(0xFF4B5563))
                            Text("+90 850 000 00 00", color = Color(0xFF4B5563))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {}, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) { Text("Start Live Chat") }
                                Button(onClick = {}, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) { Text("Create Ticket") }
                            }
                        }
                    }
                }
                "Settings" -> {
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("General settings", fontWeight = FontWeight.Bold)
                            SettingSwitch("Dark mode", userSettings.darkMode) {
                                settingsVm.updateSettings(userSettings.copy(darkMode = it))
                            }
                            SettingSwitch("Biometric login", userSettings.biometricLogin) {
                                settingsVm.updateSettings(userSettings.copy(biometricLogin = it))
                            }
                            SettingSwitch("Auto-play product videos", userSettings.autoPlayProductVideos) {
                                settingsVm.updateSettings(userSettings.copy(autoPlayProductVideos = it))
                            }
                            DividerThin()
                            Text("Notification preferences", fontWeight = FontWeight.Bold)
                            SettingSwitch("Order updates", userSettings.orderUpdates) {
                                settingsVm.updateSettings(userSettings.copy(orderUpdates = it))
                            }
                            SettingSwitch("Campaign notifications", userSettings.campaignNotifications) {
                                settingsVm.updateSettings(userSettings.copy(campaignNotifications = it))
                            }
                            SettingSwitch("Price drop alerts", userSettings.priceDropAlerts) {
                                settingsVm.updateSettings(userSettings.copy(priceDropAlerts = it))
                            }
                            SettingSwitch("Weekly digest", userSettings.weeklyDigest) {
                                settingsVm.updateSettings(userSettings.copy(weeklyDigest = it))
                            }
                            SettingSwitch("SMS notifications", userSettings.smsNotifications) {
                                settingsVm.updateSettings(userSettings.copy(smsNotifications = it))
                            }
                            if (message != null) {
                                Text(message!!, color = Color(0xFF16A34A), style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = { settingsVm.saveSettings() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !loading,
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Text("Save Settings")
                                }
                            }
                        }
                    }
                }
                else -> Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(20.dp),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun formatNotificationDate(ms: Long): String {
    if (ms <= 0L) return "Unknown date"
    val date = Date(ms)
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.US)
    return fmt.format(date)
}

@Composable
private fun BadgeLike(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEEF2FF), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = Color(0xFF4338CA), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChipButton(text: String, warning: Boolean = false) {
    Surface(
        onClick = {},
        shape = RoundedCornerShape(999.dp),
        color = if (warning) Color(0xFFFEF2F2) else Color(0xFFF3F4F6),
    ) {
        Text(
            text = text,
            color = if (warning) Color(0xFFDC2626) else Color(0xFF374151),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingRow(label: String, value: String, warning: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (warning) Color(0xFFDC2626) else Color(0xFF111827))
        if (value.isNotBlank()) Text(value, color = Color(0xFF6B7280))
    }
}

@Composable
private fun SecurityCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Security", fontWeight = FontWeight.Bold)
            Text("Last password update: 30 days ago", color = Color(0xFF6B7280))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipButton("Change Password")
                ChipButton("Enable 2FA")
            }
        }
    }
}

@Composable
private fun DividerThin() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
}

