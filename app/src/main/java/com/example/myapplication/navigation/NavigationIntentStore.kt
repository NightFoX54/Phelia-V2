package com.example.myapplication.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Routes offered from notification intents; consumed when [MainScaffoldNavHost] is active. */
object NavigationIntentStore {
    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()

    fun offer(route: String) {
        if (route.isNotBlank()) {
            _pendingRoute.value = route
        }
    }

    fun clear() {
        _pendingRoute.value = null
    }
}
