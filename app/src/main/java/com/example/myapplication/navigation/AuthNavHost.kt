package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.RegisterScreen
import com.example.myapplication.viewmodel.SessionViewModel

@Composable
fun AuthNavHost(sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN,
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                sessionViewModel = sessionViewModel,
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
            )
        }
        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                sessionViewModel = sessionViewModel,
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
    }
}
