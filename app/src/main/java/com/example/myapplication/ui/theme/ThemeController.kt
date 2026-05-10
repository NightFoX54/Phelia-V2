package com.example.myapplication.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global theme preference for Compose + pre-login screens. Backed by [ThemePreferenceStore].
 */
object ThemeController {
    private val _preference = MutableStateFlow(ThemePreference.SYSTEM)
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    fun init(context: Context) {
        _preference.value = ThemePreferenceStore(context).read()
    }

    /** Persist and broadcast theme change (login screen, settings, etc.). */
    fun set(context: Context, theme: ThemePreference) {
        ThemePreferenceStore(context).write(theme)
        _preference.value = theme
    }

    fun syncFromRemote(theme: ThemePreference) {
        _preference.value = theme
    }
}
