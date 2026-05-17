package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.ThemeController

/** Theme-aware gradient for the curved auth header. Stays within the indigo / violet brand palette. */
@Composable
internal fun rememberAuthHeaderGradient(): Brush {
    val systemDark = isSystemInDarkTheme()
    var pref by remember { mutableStateOf(ThemeController.preference.value) }
    LaunchedEffect(Unit) {
        ThemeController.preference.collect { pref = it }
    }
    val dark = pref.isDark(systemDark)
    return remember(dark) {
        if (dark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFF1E1B4B),
                    Color(0xFF3730A3),
                    Color(0xFF5B21B6),
                ),
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color(0xFF4F46E5),
                    Color(0xFF6D28D9),
                    Color(0xFF8B5CF6),
                ),
            )
        }
    }
}

/** Smooth single-dip curve at the bottom of the header. Keeps the aesthetic clean and uncrowded. */
private class WaveBottomShape(private val curveDepth: Float = 1.10f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val anchor = h * 0.84f
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, anchor)
            cubicTo(
                w * 0.78f, anchor,
                w * 0.58f, h * curveDepth,
                w * 0.42f, h * (curveDepth - 0.06f),
            )
            cubicTo(
                w * 0.24f, h * (curveDepth - 0.12f),
                w * 0.10f, h * (curveDepth - 0.02f),
                0f, anchor - h * 0.04f,
            )
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Curved gradient header with the Phelia logo. Inspired by the reference image's
 * wave-cut top section. The logo sits centered; no marketing copy.
 */
@Composable
internal fun AuthWaveHeader(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 260.dp,
    logoSize: androidx.compose.ui.unit.Dp = 76.dp,
) {
    val gradient = rememberAuthHeaderGradient()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(WaveBottomShape())
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.phelia_auth_logo),
                    contentDescription = "Phelia",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Phelia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }
    }
}

/** Title with a short accent underline – matches the reference's "Sign in" treatment. */
@Composable
internal fun AuthScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

/**
 * Minimal labeled text field. Transparent fill, soft outline, leading icon,
 * password visibility toggle when applicable.
 */
@Composable
internal fun AuthMinimalField(
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { showPassword = !showPassword }, enabled = enabled) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            visualTransformation =
                if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        )
    }
}
