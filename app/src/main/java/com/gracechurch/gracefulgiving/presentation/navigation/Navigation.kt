package com.gracechurch.gracefulgiving.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.gracechurch.gracefulgiving.presentation.screens.home.LoginScreen
import com.gracechurch.gracefulgiving.presentation.screens.detail.BatchEntryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUserId by remember { mutableStateOf(0L) }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            // Simply omit onNavigateToRegister when calling, since it has a default
            LoginScreen(
                onLoginSuccess = {
                    currentUserId = 1L
                    navController.navigate("batch") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("batch") {
            BatchEntryScreen(userId = currentUserId)
        }
    }
}
