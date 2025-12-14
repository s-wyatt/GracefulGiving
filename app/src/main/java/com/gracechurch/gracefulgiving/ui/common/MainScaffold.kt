package com.gracechurch.gracefulgiving.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gracechurch.gracefulgiving.app.navigation.AppNavGraph
import com.gracechurch.gracefulgiving.app.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(userId: Long) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = currentRoute == Routes.DASHBOARD,
                    onClick = { navController.navigate(Routes.DASHBOARD); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Batch Management") },
                    selected = currentRoute == Routes.BATCH_MANAGEMENT,
                    onClick = { navController.navigate(Routes.BATCH_MANAGEMENT); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Donors") },
                    selected = currentRoute == Routes.DONORS_DONATIONS,
                    onClick = { navController.navigate(Routes.DONORS_DONATIONS); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Yearly Giving Statements") },
                    selected = currentRoute == Routes.YEARLY_STATEMENTS,
                    onClick = { navController.navigate(Routes.YEARLY_STATEMENTS); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Bank Settings") },
                    selected = currentRoute == Routes.BANK_SETTINGS,
                    onClick = { navController.navigate(Routes.BANK_SETTINGS); scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Graceful Giving") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation drawer"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavGraph(navController = navController, userId = userId)
            }
        }
    }
}