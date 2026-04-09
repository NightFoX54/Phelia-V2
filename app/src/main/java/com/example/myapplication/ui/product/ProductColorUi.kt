package com.example.myapplication.ui.product

import androidx.compose.ui.graphics.Color

/**
 * For color attribute values:
 * - `#RRGGBB` or `#AARRGGBB` (shown as swatch)
 * - optional `Label|#RRGGBB` (label + swatch)
 */
fun parseComposeColorHex(raw: String): Color? {
    val s = raw.trim()
    if (!s.startsWith("#")) return null
    return try {
        val c = android.graphics.Color.parseColor(s)
        Color(c)
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun colorLabelAndComposeColor(raw: String): Pair<String, Color?> {
    val trimmed = raw.trim()
    if (trimmed.contains("|")) {
        val parts = trimmed.split("|", limit = 2)
        val label = parts[0].trim()
        val rest = parts.getOrNull(1)?.trim().orEmpty()
        val color = parseComposeColorHex(rest)
        return (label.ifBlank { rest }) to color
    }
    val c = parseComposeColorHex(trimmed)
    return (trimmed.ifBlank { "" }) to c
}

fun isColorAttributeKey(key: String): Boolean = key.equals("color", ignoreCase = true)
