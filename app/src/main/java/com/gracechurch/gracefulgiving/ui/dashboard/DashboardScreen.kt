package com.gracechurch.gracefulgiving.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Count/Total Donations")
        Text("  Month To Date: ${uiState.monthToDateTotal}")
        Text("Quarter To Date: ${uiState.quarterToDateTotal}")
        Text("   Year To Date: ${uiState.yearToDateTotal}")

        Spacer(modifier = Modifier.height(20.dp))

        Text("Open Batches:")
        uiState.openBatches.forEach { batch ->
            Text("${batch.batchName}: ${batch.total}")
        }
    }
}