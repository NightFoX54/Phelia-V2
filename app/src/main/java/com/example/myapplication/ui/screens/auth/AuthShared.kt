package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.ui.theme.ThemeController
import com.example.myapplication.ui.theme.ThemePreference

@Composable
internal fun rememberAuthScreenBackgroundBrush(): Brush {
    val systemDark = isSystemInDarkTheme()
    var pref by remember { mutableStateOf(ThemeController.preference.value) }
    LaunchedEffect(Unit) {
        ThemeController.preference.collect { pref = it }
    }
    val dark = pref.isDark(systemDark)
    return remember(dark) {
        if (dark) {
            Brush.linearGradient(
                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81)),
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFFE0E7FF), Color(0xFFF5F3FF), Color(0xFFFCE7F3)),
            )
        }
    }
}

@Composable
internal fun AuthLabeledField(
    label: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
) {
    var showPassword by remember { mutableStateOf(false) }
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { showPassword = !showPassword }, enabled = enabled) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            visualTransformation =
                if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        )
    }
}
