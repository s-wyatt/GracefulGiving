package com.gracechurch.gracefulgiving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                // The root composable that handles the entire app's navigation flow.
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
                onLoginSuccess = {
                    // After successful login, navigate to the main app, clearing the login flow.
                    navController.navigate("main_app") {
                        popUpTo("login_flow") { inclusive = true }
                    }
                }
            )
        }
        composable("main_app") {
            // The MainScaffold contains the TopAppBar, Drawer, and the main AppNavGraph.
            MainScaffold()
        }
    }
}
