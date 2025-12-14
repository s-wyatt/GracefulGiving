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
import com.gracechurch.gracefulgiving.ui.donor.DonorsDonationsScreen
import com.gracechurch.gracefulgiving.ui.statements.YearlyStatementsScreen

@Composable
fun AppNavGraph(navController: NavHostController, userId: Long) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(navController)
        }
        composable(Routes.BATCH_MANAGEMENT) {
            BatchManagementScreen(navController, userId)
        }
        composable(Routes.DONORS_DONATIONS) {
            DonorsDonationsScreen(navController)
        }
        composable(Routes.BANK_SETTINGS) {
            BankSettingsScreen()
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
                batchId = batchId
            )
        }
    }
}
