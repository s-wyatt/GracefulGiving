package com.gracechurch.gracefulgiving.app.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gracechurch.gracefulgiving.ui.batch.BatchEntryScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchSelectionScreen
import com.gracechurch.gracefulgiving.ui.donation.DonationScreen
import com.gracechurch.gracefulgiving.ui.donor.DonorDetailScreen
import com.gracechurch.gracefulgiving.ui.donor.DonorListScreen
import com.gracechurch.gracefulgiving.ui.login.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                navController = navController,
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
                onNavigateUp = {
                    navController.popBackStack()
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
                batchId = batchId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DONORS_DONATIONS) {
            DonorListScreen(navController)
        }

        composable(
            route = "donor_detail/{donorId}",
            arguments = listOf(navArgument("donorId") { type = NavType.LongType })
        ) { backStackEntry ->
            val donorId = backStackEntry.arguments?.getLong("donorId") ?: 0L
            DonorDetailScreen(donorId = donorId, navController = navController)
        }

        composable(
            route = "donation/{donationId}",
            arguments = listOf(navArgument("donationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val donationId = backStackEntry.arguments?.getLong("donationId") ?: 0L
            DonationScreen(donationId = donationId, navController = navController)
        }
    }
}
