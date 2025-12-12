package com.gracechurch.gracefulgiving.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gracechurch.gracefulgiving.ui.batch.BatchEntryScreen
import com.gracechurch.gracefulgiving.ui.bank.BankSettingsScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchManagementScreen
import com.gracechurch.gracefulgiving.ui.dashboard.DashboardScreen
import com.gracechurch.gracefulgiving.ui.donors.DonorsDonationsScreen
import com.gracechurch.gracefulgiving.ui.statements.YearlyStatementsScreen

// GENTLE FIX: The Routes object has been moved to its own file (Routes.kt)
// and is no longer needed here.

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(navController)
        }
        composable(Routes.BATCH_MANAGEMENT) {
            BatchManagementScreen(navController)
        }
        composable(Routes.DONORS_DONATIONS) {
            DonorsDonationsScreen(navController)
        }
        composable(Routes.BANK_SETTINGS) {
            // This now resolves correctly because of the added import
            BankSettingsScreen(navController)
        }
        composable(Routes.YEARLY_STATEMENTS) {
            YearlyStatementsScreen(navController)
        }

        // The destination for the Batch Entry screen
        composable(
            route = "${Routes.BATCH_ENTRY}/{batchId}",
            arguments = listOf(navArgument("batchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
            BatchEntryScreen(
                batchId = batchId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
