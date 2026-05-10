package com.example.myapplication.ui.theme

import android.content.Context

class ThemePreferenceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): ThemePreference {
        val raw = prefs.getString(KEY_THEME, null)
        return ThemePreference.fromStorage(raw) ?: ThemePreference.SYSTEM
    }

    fun write(theme: ThemePreference) {
        prefs.edit().putString(KEY_THEME, theme.storageKey).apply()
    }

    private companion object {
        const val PREFS_NAME = "app_theme_prefs"
        const val KEY_THEME = "theme_preference"
    }
}
