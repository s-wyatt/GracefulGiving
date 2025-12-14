package com.gracechurch.gracefulgiving.app.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gracechurch.gracefulgiving.ui.bank.BankSettingsScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchEntryScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchSelectionScreen
import com.gracechurch.gracefulgiving.ui.login.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // We don't need to store the user ID here anymore, as it will be passed through navigation
    // var currentUserId by remember { mutableStateOf(0L) }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userId ->
                    // Navigate to batch selection screen with the user's ID
                    navController.navigate("batch_selection/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // The new BatchSelectionScreen route
        composable(
            route = "batch_selection/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            BatchSelectionScreen(
                userId = userId,
                onNavigateToBatchEntry = { batchId ->
                    // Navigate to the specific batch entry screen
                    navController.navigate("batch/$batchId")
                },
                onNavigateToSettings = {
                    navController.navigate("bank_settings")
                }
            )
        }

        // The existing BatchEntryScreen route, now taking batchId
        composable(
            route = "batch/{batchId}",
            arguments = listOf(navArgument("batchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
            BatchEntryScreen(
                batchId = batchId // Pass the batchId to the screen
            )
        }

        composable("bank_settings") {
            BankSettingsScreen()
        }
    }
}
