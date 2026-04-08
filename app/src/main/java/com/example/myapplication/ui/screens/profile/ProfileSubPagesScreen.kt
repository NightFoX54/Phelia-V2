package com.example.myapplication.ui.screens.profile

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppTopBar

@Composable
fun ProfileSubPagesScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("user@test.com") }
    var phone by remember { mutableStateOf("+90 5XX XXX XX XX") }
    var bio by remember { mutableStateOf("Teknoloji ve kampanya avcısı.") }
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
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Save Changes") }
                        }
                    }
                    SecurityCard()
                }
                "Notifications" -> {
                    var orderUpdates by remember { mutableStateOf(true) }
                    var campaign by remember { mutableStateOf(true) }
                    var priceDrop by remember { mutableStateOf(true) }
                    var digest by remember { mutableStateOf(false) }
                    var sms by remember { mutableStateOf(false) }
                    listOf(
                        "Order updates" to Pair(orderUpdates) { v: Boolean -> orderUpdates = v },
                        "Campaign notifications" to Pair(campaign) { v: Boolean -> campaign = v },
                        "Price drop alerts" to Pair(priceDrop) { v: Boolean -> priceDrop = v },
                        "Weekly digest" to Pair(digest) { v: Boolean -> digest = v },
                        "SMS notifications" to Pair(sms) { v: Boolean -> sms = v },
                    ).forEach { (label, pair) ->
                        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label)
                                Switch(checked = pair.first, onCheckedChange = pair.second)
                            }
                        }
                    }
                    Button(onClick = {}, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("Save Notification Preferences")
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
                    var darkMode by remember { mutableStateOf(false) }
                    var biometric by remember { mutableStateOf(true) }
                    var autoPlay by remember { mutableStateOf(false) }
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("App Settings", fontWeight = FontWeight.Bold)
                            SettingSwitch("Dark mode", darkMode) { darkMode = it }
                            SettingSwitch("Biometric login", biometric) { biometric = it }
                            SettingSwitch("Auto-play product videos", autoPlay) { autoPlay = it }
                            DividerThin()
                            SettingRow("Language", "English")
                            SettingRow("Currency", "USD")
                            SettingRow("Region", "United States")
                        }
                    }
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Privacy & Security", fontWeight = FontWeight.Bold)
                            SettingRow("Change password", "")
                            SettingRow("Two-factor authentication", "Enabled")
                            SettingRow("Manage devices", "2 active sessions")
                            SettingRow("Download my data", "")
                            SettingRow("Delete account", "", warning = true)
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

