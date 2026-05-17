package com.example.myapplication.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

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
    var taxNumber by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pickedLogoUri by remember { mutableStateOf<Uri?>(null) }

    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var storeApplySuccess by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        if (email.length < 5 || !email.contains("@")) return@LaunchedEffect
        delay(600)
        sessionViewModel.checkEmailAvailability(email) { result ->
            result.fold(
                onSuccess = {
                    if (error == "Choose another mail address") error = null
                },
                onFailure = { e ->
                    if (e.message == "Choose another mail address") error = e.message
                },
            )
        }
    }

    LaunchedEffect(storeName) {
        if (storeName.isBlank()) return@LaunchedEffect
        delay(600)
        sessionViewModel.checkStoreNameAvailability(storeName) { result ->
            result.fold(
                onSuccess = {
                    if (error == "Please select different store name") error = null
                },
                onFailure = { e ->
                    if (e.message == "Please select different store name") error = e.message
                },
            )
        }
    }

    val pickLogo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? -> pickedLogoUri = uri }

    val title = when {
        kind == RegisterKind.Customer -> "Create account"
        storeStep == 0 -> "Owner details"
        else -> "Store details"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            AuthWaveHeader(height = 210.dp, logoSize = 64.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 10.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AuthScreenTitle(title = title)

                AuthSegmented(
                    options = listOf("Customer", "Store"),
                    selectedIndex = if (kind == RegisterKind.Customer) 0 else 1,
                    enabled = !busy,
                    onSelect = { idx ->
                        kind = if (idx == 0) RegisterKind.Customer else RegisterKind.StoreApplication
                        storeStep = 0
                        error = null
                        storeApplySuccess = false
                    },
                )

                if (kind == RegisterKind.StoreApplication) {
                    Text(
                        text = "Step ${storeStep + 1} of 2",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                when {
                    kind == RegisterKind.Customer -> {
                        AuthMinimalField(
                            label = "Full name",
                            icon = Icons.Outlined.Person,
                            value = name,
                            onValueChange = { name = it; error = null },
                            placeholder = "John Doe",
                            enabled = !busy,
                        )
                        AuthMinimalField(
                            label = "Email",
                            icon = Icons.Outlined.Email,
                            value = email,
                            onValueChange = { email = it; error = null },
                            placeholder = "you@email.com",
                            enabled = !busy,
                        )
                        AuthMinimalField(
                            label = "Password",
                            icon = Icons.Outlined.Lock,
                            value = password,
                            onValueChange = { password = it; error = null },
                            placeholder = "At least 6 characters",
                            enabled = !busy,
                            isPassword = true,
                        )
                        AuthMinimalField(
                            label = "Confirm password",
                            icon = Icons.Outlined.Lock,
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; error = null },
                            placeholder = "Repeat password",
                            enabled = !busy,
                            isPassword = true,
                        )

                        InlineError(error)

                        AuthPrimaryButton(
                            text = if (busy) "Creating account…" else "Create account",
                            enabled = !busy,
                        ) {
                            error = null
                            val validation = validateCustomer(name, email, password, confirmPassword)
                            if (validation != null) {
                                error = validation
                                return@AuthPrimaryButton
                            }
                            busy = true
                            sessionViewModel.checkEmailAvailability(email) { availResult ->
                                availResult.fold(
                                    onSuccess = {
                                        sessionViewModel.register(name, email, password) { result ->
                                            busy = false
                                            result.fold(
                                                onSuccess = { },
                                                onFailure = { e -> error = e.message },
                                            )
                                        }
                                    },
                                    onFailure = { e ->
                                        busy = false
                                        error = e.message
                                    },
                                )
                            }
                        }
                    }

                    storeStep == 0 -> {
                        AuthMinimalField(
                            label = "Full name",
                            icon = Icons.Outlined.Person,
                            value = name,
                            onValueChange = { name = it; error = null },
                            placeholder = "John Doe",
                            enabled = !busy,
                        )
                        AuthMinimalField(
                            label = "Email",
                            icon = Icons.Outlined.Email,
                            value = email,
                            onValueChange = { email = it; error = null },
                            placeholder = "you@email.com",
                            enabled = !busy,
                        )
                        AuthMinimalField(
                            label = "Password",
                            icon = Icons.Outlined.Lock,
                            value = password,
                            onValueChange = { password = it; error = null },
                            placeholder = "At least 6 characters",
                            enabled = !busy,
                            isPassword = true,
                        )
                        AuthMinimalField(
                            label = "Confirm password",
                            icon = Icons.Outlined.Lock,
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; error = null },
                            placeholder = "Repeat password",
                            enabled = !busy,
                            isPassword = true,
                        )

                        InlineError(error)

                        AuthPrimaryButton(
                            text = if (busy) "Checking…" else "Continue",
                            enabled = !busy,
                        ) {
                            error = null
                            val validation = validateCustomer(name, email, password, confirmPassword)
                            if (validation != null) {
                                error = validation
                                return@AuthPrimaryButton
                            }
                            busy = true
                            sessionViewModel.checkEmailAvailability(email) { result ->
                                busy = false
                                result.fold(
                                    onSuccess = { storeStep = 1 },
                                    onFailure = { e -> error = e.message },
                                )
                            }
                        }
                    }

                    else -> {
                        MinimalOutlinedField(
                            value = storeName,
                            onValueChange = { storeName = it; error = null },
                            label = "Store name",
                            leadingIcon = Icons.Outlined.Storefront,
                            enabled = !busy,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MinimalOutlinedField(
                                value = taxNumber,
                                onValueChange = { taxNumber = it },
                                label = "Tax number",
                                enabled = !busy,
                                modifier = Modifier.weight(1f),
                            )
                            MinimalOutlinedField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Phone",
                                enabled = !busy,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        MinimalOutlinedField(
                            value = businessAddress,
                            onValueChange = { businessAddress = it },
                            label = "Business address",
                            enabled = !busy,
                            minLines = 2,
                        )
                        MinimalOutlinedField(
                            value = storeDescription,
                            onValueChange = { storeDescription = it; error = null },
                            label = "Store description",
                            enabled = !busy,
                            minLines = 3,
                        )

                        StoreLogoPicker(
                            uri = pickedLogoUri,
                            enabled = !busy,
                            onPick = {
                                pickLogo.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )

                        if (storeApplySuccess) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                                    Text(
                                        text = "Application submitted. We'll be in touch.",
                                        color = Color(0xFF065F46),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        InlineError(error)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = { storeStep = 0 },
                                enabled = !busy,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(56.dp),
                            ) { Text("Back", fontWeight = FontWeight.SemiBold) }

                            Button(
                                onClick = {
                                    error = null
                                    if (storeName.isBlank()) {
                                        error = "Store name is required"; return@Button
                                    }
                                    if (taxNumber.isBlank()) {
                                        error = "Tax number is required"; return@Button
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
                                        applicantPhone = phone,
                                        taxNumber = taxNumber,
                                        businessAddress = businessAddress,
                                    ) { result ->
                                        busy = false
                                        result.fold(
                                            onSuccess = { storeApplySuccess = true },
                                            onFailure = { e -> error = e.message },
                                        )
                                    }
                                },
                                enabled = !busy && !storeApplySuccess,
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (storeApplySuccess) Color(0xFF10B981)
                                    else MaterialTheme.colorScheme.primary,
                                ),
                                modifier = Modifier.weight(1f).height(56.dp),
                            ) {
                                if (busy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = if (storeApplySuccess) "Submitted" else "Apply",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                        if (busy) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = " Sign in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(enabled = !busy) { onNavigateToLogin() },
                    )
                }
            }
        }

        AuthCompactThemeToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 12.dp),
        )
    }
}

private fun validateCustomer(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
): String? {
    if (name.isBlank()) return "Full name is required"
    if (email.isBlank()) return "Email is required"
    if (password.length < 6) return "Password must be at least 6 characters"
    if (password != confirmPassword) return "Passwords do not match"
    return null
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InlineError(error: String?) {
    if (error == null) return
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.30f)),
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun MinimalOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun StoreLogoPicker(
    uri: Uri?,
    enabled: Boolean,
    onPick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(20.dp),
                )
                .clickable(enabled = enabled, onClick = onPick),
            contentAlignment = Alignment.Center,
        ) {
            when (uri) {
                null -> Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = "Add logo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(30.dp),
                )
                else -> AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Text(
            text = if (uri == null) "Add store logo" else "Tap to change",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthSegmented(
    options: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val interaction = remember { MutableInteractionSource() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surface
                        else Color.Transparent,
                    )
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                        onClick = { onSelect(index) },
                    ),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
