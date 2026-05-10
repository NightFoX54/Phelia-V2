package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.ThemeController
import com.example.myapplication.ui.theme.ThemePreference

/** Compact sun / switch / moon for corners on auth screens. */
@Composable
fun AuthCompactThemeToggle(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val systemDark = isSystemInDarkTheme()
    var selected by remember { mutableStateOf(ThemeController.preference.value) }
    LaunchedEffect(Unit) {
        ThemeController.preference.collect { selected = it }
    }

    val effectiveDark = when (selected) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> systemDark
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text("☀️", fontSize = 13.sp)
        Switch(
            checked = effectiveDark,
            onCheckedChange = { nowDark ->
                ThemeController.set(
                    context,
                    if (nowDark) ThemePreference.DARK else ThemePreference.LIGHT,
                )
            },
            modifier = Modifier.scale(0.82f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        Text("🌙", fontSize = 13.sp)
    }
}

@Composable
fun AuthThemePreferenceRow(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            AuthCompactThemeToggle()
        }
    }
}
