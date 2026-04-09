package com.example.myapplication.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.viewmodel.SessionViewModel

private enum class RegisterKind { Customer, StoreApplication }

@Composable
fun RegisterScreen(
    sessionViewModel: SessionViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var kind by remember { mutableStateOf(RegisterKind.Customer) }
    var storeStep by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var storeName by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }
    var pickedLogoUri by remember { mutableStateOf<Uri?>(null) }

    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var storeApplySuccess by remember { mutableStateOf(false) }

    val pickLogo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? -> pickedLogoUri = uri }

    val bg = Brush.linearGradient(listOf(Color(0xFFE0E7FF), Color(0xFFF5F3FF), Color(0xFFFCE7F3)))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                text = "Create Account",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KindChip(
                    label = "Customer",
                    selected = kind == RegisterKind.Customer,
                    onClick = {
                        kind = RegisterKind.Customer
                        storeStep = 0
                        error = null
                        storeApplySuccess = false
                    },
                    modifier = Modifier.weight(1f),
                )
                KindChip(
                    label = "Open a store",
                    selected = kind == RegisterKind.StoreApplication,
                    onClick = {
                        kind = RegisterKind.StoreApplication
                        storeStep = 0
                        error = null
                        storeApplySuccess = false
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = when (kind) {
                    RegisterKind.Customer -> "Register to shop on the marketplace."
                    RegisterKind.StoreApplication -> when (storeStep) {
                        0 -> "Step 1 of 2 — your account"
                        else -> "Step 2 of 2 — your store (pending admin approval)"
                    }
                },
                color = Color(0xFF4B5563),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when {
                        kind == RegisterKind.Customer -> {
                            AuthLabeledField(
                                label = "Full Name",
                                icon = Icons.Default.Person,
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "John Doe",
                            )
                            AuthLabeledField(
                                label = "Email Address",
                                icon = Icons.Default.Email,
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "your@email.com",
                            )
                            AuthLabeledField(
                                label = "Password",
                                icon = Icons.Default.Lock,
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "••••••••",
                            )
                            AuthLabeledField(
                                label = "Confirm Password",
                                icon = Icons.Default.Lock,
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = "••••••••",
                            )
                            SubmitButton(
                                busy = busy,
                                text = if (busy) "Creating account…" else "Create Account",
                                onClick = {
                                    error = null
                                    if (password != confirmPassword) {
                                        error = "Passwords do not match"
                                        return@SubmitButton
                                    }
                                    busy = true
                                    sessionViewModel.register(name, email, password) { result ->
                                        busy = false
                                        result.fold(onSuccess = { }, onFailure = { e -> error = e.message })
                                    }
                                },
                            )
                        }
                        storeStep == 0 -> {
                            AuthLabeledField(
                                label = "Full Name",
                                icon = Icons.Default.Person,
                                value = name,
                                onValueChange = { name = it },
                                placeholder = "John Doe",
                            )
                            AuthLabeledField(
                                label = "Email Address",
                                icon = Icons.Default.Email,
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "your@email.com",
                            )
                            AuthLabeledField(
                                label = "Password",
                                icon = Icons.Default.Lock,
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "••••••••",
                            )
                            AuthLabeledField(
                                label = "Confirm Password",
                                icon = Icons.Default.Lock,
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = "••••••••",
                            )
                            Button(
                                onClick = {
                                    error = null
                                    if (password != confirmPassword) {
                                        error = "Passwords do not match"
                                        return@Button
                                    }
                                    if (name.isBlank() || email.isBlank() || password.length < 6) {
                                        error = "Fill all fields; password at least 6 characters."
                                        return@Button
                                    }
                                    storeStep = 1
                                },
                                enabled = !busy,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                            ) {
                                Text("Continue", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        else -> {
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                label = { Text("Store name") },
                                leadingIcon = { Icon(Icons.Default.Storefront, null) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy,
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                            )
                            OutlinedTextField(
                                value = storeDescription,
                                onValueChange = { storeDescription = it },
                                label = { Text("Store description") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy,
                                minLines = 3,
                                shape = RoundedCornerShape(14.dp),
                            )
                            Text(
                                "Store logo (1:1 center crop after upload)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center,
                            ) {
                                when (val u = pickedLogoUri) {
                                    null -> Icon(Icons.Default.Image, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(40.dp))
                                    else -> AsyncImage(
                                        model = u,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    pickLogo.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                    )
                                },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Choose logo")
                            }
                            if (storeApplySuccess) {
                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                ) {
                                    Text(
                                        "Account created. Your store application is pending admin review. You can sign in as a customer until it is approved.",
                                        color = Color(0xFF166534),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { storeStep = 0 },
                                    enabled = !busy,
                                    modifier = Modifier.weight(1f),
                                ) { Text("Back") }
                                Button(
                                    onClick = {
                                        error = null
                                        if (storeName.isBlank()) {
                                            error = "Store name is required"
                                            return@Button
                                        }
                                        busy = true
                                        storeApplySuccess = false
                                        sessionViewModel.registerStoreApplication(
                                            context = context,
                                            name = name,
                                            email = email,
                                            password = password,
                                            storeName = storeName,
                                            storeDescription = storeDescription,
                                            localLogoUri = pickedLogoUri,
                                        ) { result ->
                                            busy = false
                                            result.fold(
                                                onSuccess = { storeApplySuccess = true },
                                                onFailure = { e -> error = e.message },
                                            )
                                        }
                                    },
                                    enabled = !busy,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(if (busy) "Submitting…" else "Submit application")
                                }
                            }
                            if (busy) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    Text(
                        text = "Already have an account? Sign in",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                            .clickable(enabled = !busy) { onNavigateToLogin() },
                    )

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
                }
            }
        }
    }
}

@Composable
private fun KindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.White
    val fg = if (selected) Color.White else Color(0xFF374151)
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SubmitButton(
    busy: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !busy,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
