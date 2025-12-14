package com.gracechurch.gracefulgiving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gracechurch.gracefulgiving.presentation.screens.home.LoginScreen
import com.gracechurch.gracefulgiving.ui.common.MainScaffold
import com.gracefulgiving.gracefulgiving.theme.GracefulGivingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GracefulGivingTheme {
                GracefulGivingApp()
            }
        }
    }
}

@Composable
fun GracefulGivingApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login_flow") {
        composable("login_flow") {
            LoginScreen(
                onLoginSuccess = { userId ->
                    navController.navigate("main_app/$userId") {
                        popUpTo("login_flow") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "main_app/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            MainScaffold(userId = userId)
        }
    }
}
