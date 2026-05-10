package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.navigation.MyAppNavHost
import com.example.myapplication.navigation.NavigationIntentStore
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemeController
import com.example.myapplication.ui.theme.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeController.init(applicationContext)
        dispatchIntentRoute(intent)
        setContent {
            var pref by remember { mutableStateOf(ThemeController.preference.value) }
            LaunchedEffect(Unit) {
                ThemeController.preference.collect { pref = it }
            }
            MyApplicationTheme(darkTheme = pref.isDark(isSystemInDarkTheme())) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MyAppNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchIntentRoute(intent)
    }

    private fun dispatchIntentRoute(intent: Intent?) {
        val route = intent?.getStringExtra(EXTRA_PENDING_NAV_ROUTE).orEmpty()
        if (route.isNotBlank()) {
            NavigationIntentStore.offer(route)
        }
    }

    companion object {
        const val EXTRA_PENDING_NAV_ROUTE = "extra_pending_nav_route"
    }
}
