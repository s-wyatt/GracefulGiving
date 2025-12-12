package com.gracechurch.gracefulgiving.ui.statements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyStatementsScreen(
    navController: NavController,
    vm: YearlyStatementsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadDonors()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Yearly Giving Statements") })
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.donors) { donorWithDonations ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.generateYearlyStatement(donorWithDonations.donor.donorId)
                                }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${donorWithDonations.donor.firstName} ${donorWithDonations.donor.lastName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${donorWithDonations.donations.size} donations",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Tap to generate & print yearly statement",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            state.successMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = { navController.navigate(Routes.DASHBOARD) }) {
                Text("Back to Dashboard")
            }
        }
    }
}
