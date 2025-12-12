package com.gracechurch.gracefulgiving.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gracechurch.gracefulgiving.app.navigation.AppNavGraph
import com.gracechurch.gracefulgiving.app.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Get the current route to update the TopAppBar title
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.DASHBOARD

    // This is the main UI shell for the authenticated user.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController = navController) {
                // Close the drawer after a navigation event
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Dynamically set the title based on the current screen
                        Text(currentRoute.toTitleCase())
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // The AppNavGraph contains all the main screens of the app.
                AppNavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun AppDrawerContent(navController: NavHostController, closeDrawer: () -> Unit) {
    // Assuming isAdmin logic will be passed down or retrieved from a ViewModel later
    val isAdmin = true

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Navigation", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(12.dp))
            Spacer(Modifier.height(8.dp))
            NavigationDrawerItem(
                label = { Text("Dashboard") },
                selected = false,
                onClick = {
                    navController.navigate(Routes.DASHBOARD)
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                label = { Text("Batch Management") },
                selected = false,
                onClick = {
                    navController.navigate(Routes.BATCH_MANAGEMENT)
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                label = { Text("Donors & Donations") },
                selected = false,
                onClick = {
                    navController.navigate(Routes.DONORS_DONATIONS)
                    closeDrawer()
                }
            )
            if (isAdmin) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Bank Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.BANK_SETTINGS)
                        closeDrawer()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Yearly Statements") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.YEARLY_STATEMENTS)
                        closeDrawer()
                    }
                )
            }
        }
    }
}

// Helper function to format route names for display
private fun String.toTitleCase(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .replace('_', ' ')
}
