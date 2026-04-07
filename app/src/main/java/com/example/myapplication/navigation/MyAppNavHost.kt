package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.auth.ProfileErrorScreen
import com.example.myapplication.viewmodel.SessionState
import com.example.myapplication.viewmodel.SessionViewModel

@Composable
fun MyAppNavHost() {
    val sessionVm: SessionViewModel = viewModel()
    val sessionState by sessionVm.sessionState.collectAsState()

    when (val s = sessionState) {
        SessionState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        SessionState.SignedOut -> AuthNavHost(sessionViewModel = sessionVm)
        is SessionState.SignedIn -> MainScaffoldNavHost(sessionViewModel = sessionVm)
        is SessionState.ProfileError -> ProfileErrorScreen(
            message = s.message,
            sessionViewModel = sessionVm,
        )
    }
}
