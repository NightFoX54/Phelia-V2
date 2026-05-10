package com.example.myapplication.ui.theme

enum class ThemePreference(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    fun isDark(systemIsDark: Boolean): Boolean =
        when (this) {
            SYSTEM -> systemIsDark
            LIGHT -> false
            DARK -> true
        }

    companion object {
        fun fromStorage(value: String?): ThemePreference? =
            entries.firstOrNull { it.storageKey == value }

        fun migrateFromLegacyDarkMode(darkMode: Boolean?): ThemePreference =
            if (darkMode == true) DARK else SYSTEM
    }
}
