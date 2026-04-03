package com.example.myapplication.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodels.SessionViewModel

@Composable
fun AuthScreen(
    onDone: () -> Unit,
    sessionViewModel: SessionViewModel,
    modifier: Modifier = Modifier,
) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("user@test.com") }
    var password by remember { mutableStateOf("123456") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val bg = Brush.linearGradient(listOf(Color(0xFFE0E7FF), Color(0xFFF5F3FF), Color(0xFFFCE7F3)))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text(
                text = if (isLogin) "Welcome Back" else "Create Account",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = if (isLogin) "Sign in to continue shopping" else "Create an account to get started",
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tabs
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            TabButton(
                                text = "Login",
                                selected = isLogin,
                                onClick = { isLogin = true; error = null },
                                modifier = Modifier.weight(1f),
                            )
                            TabButton(
                                text = "Register",
                                selected = !isLogin,
                                onClick = { isLogin = false; error = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (!isLogin) {
                        LabeledField(
                            label = "Full Name",
                            icon = Icons.Default.Person,
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "John Doe",
                        )
                    }
                    LabeledField(
                        label = "Email Address",
                        icon = Icons.Default.Email,
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "your@email.com",
                    )
                    LabeledField(
                        label = "Password",
                        icon = Icons.Default.Lock,
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••••",
                    )
                    if (!isLogin) {
                        LabeledField(
                            label = "Confirm Password",
                            icon = Icons.Default.Lock,
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "••••••••",
                        )
                    }

                    Button(
                        onClick = {
                            error = null
                            if (isLogin) {
                                val ok = sessionViewModel.login(email, password)
                                if (ok) onDone() else error = "Invalid email or password"
                            } else {
                                if (password != confirmPassword) {
                                    error = "Passwords do not match"
                                } else {
                                    onDone()
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text(if (isLogin) "Sign In" else "Create Account", fontWeight = FontWeight.SemiBold)
                    }

                    if (error != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        ) {
                            Text(
                                text = error!!,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                            )
                        }
                    }

                    if (isLogin) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text("Demo Credentials:", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E3A8A), style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Customer: user@test.com / 123456", color = Color(0xFF1D4ED8), style = MaterialTheme.typography.bodySmall)
                                Text("Store Owner: store@test.com / 123456", color = Color(0xFF1D4ED8), style = MaterialTheme.typography.bodySmall)
                                Text("Admin: admin@test.com / 123456", color = Color(0xFF1D4ED8), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = if (selected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = if (selected) 3.dp else 0.dp,
        modifier = modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF6B7280),
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB),
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

