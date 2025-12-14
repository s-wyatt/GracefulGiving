package com.gracechurch.gracefulgiving.ui.donor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes

@Composable
fun DonorsDonationsScreen(navController: NavController, viewModel: DonorsDonationsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Donors & Donations", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate(Routes.DASHBOARD) }) {
            Text("Back to Dashboard")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Donors:")
        uiState.donors.forEach {
            Text("${it.firstName} ${it.lastName}")
        }

        Spacer(Modifier.height(20.dp))

        Text("Donations:")
        uiState.donations.forEach {
            Text("Donation ${it.donationId}: $${String.format("%.2f", it.checkAmount)}")
        }
    }
}