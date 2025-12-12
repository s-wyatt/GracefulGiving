package com.gracechurch.gracefulgiving.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("MTD: ${uiState.monthToDateTotal}")
        Text("QTD: ${uiState.quarterToDateTotal}")
        Text("YTD: ${uiState.yearToDateTotal}")

        Spacer(modifier = Modifier.height(20.dp))

        Text("Open Batches:")
        uiState.openBatches.forEach { batch ->
            Text("${batch.batchName}: ${batch.total}")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate(Routes.BATCH_MANAGEMENT) }) {
            Text("Go to Batch Management")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate(Routes.DONORS_DONATIONS) }) {
            Text("Donors / Donations")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate(Routes.BANK_SETTINGS) }) {
            Text("Bank Settings (Admin)")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate(Routes.YEARLY_STATEMENTS) }) {
            Text("Yearly Giving Statements")
        }
    }
}
