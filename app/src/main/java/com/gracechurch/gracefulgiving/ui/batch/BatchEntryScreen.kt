package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData

@Composable
fun BatchEntryScreen(
    // GENTLE FIX: The onNavigateBack parameter is no longer needed.
    // The MainScaffold provides the back arrow in its TopAppBar.
    batchId: Long,
    vm: BatchEntryViewModel = hiltViewModel()
) {

    val state by vm.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(batchId) {
        vm.loadBatch(batchId)
    }

    val batchWithDonations = state.batchWithDonations
    val donations = batchWithDonations?.donations ?: emptyList()
    val donationsCount = donations.size
    val totalAmount = donations.sumOf { it.donation.checkAmount }
    val totalAmountText = "$${"%.2f".format(totalAmount)}"

    // GENTLE FIX: Use Scaffold without a TopAppBar, as MainScaffold provides it.
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Donation")
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {

            // Summary Card
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column {
                        Text("Donations", style = MaterialTheme.typography.bodySmall)
                        Text(donationsCount.toString(), style = MaterialTheme.typography.headlineMedium)
                    }
                    Column {
                        Text("Total", style = MaterialTheme.typography.bodySmall)
                        Text(totalAmountText, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            // Scan Check Button
            Button(
                onClick = { showScanner = true },
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan Check")
                Spacer(Modifier.width(8.dp))
                Text("Scan Check")
            }

            // Donations List
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(donations, key = { it.donation.donationId }) { donationWithDonor ->
                    val donorName = "${donationWithDonor.donor.firstName} ${donationWithDonor.donor.lastName}"
                    val donationText = "Check #${donationWithDonor.donation.checkNumber} - $${"%.2f".format(donationWithDonor.donation.checkAmount)}"

                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(donorName, style = MaterialTheme.typography.titleMedium)
                            Text(donationText)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DonationEntryDialog(
            scannedData = state.scannedData,
            batchId = batchWithDonations?.batch?.batchId ?: 0L,
            batchDate = batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis(),
            onDismiss = {
                showDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img, bId ->
                vm.addDonation(fn, ln, cn, amt, dt, img, bId)
                showDialog = false
                vm.clearScannedData()
            }
        )
    }

    if (showScanner) {
        CheckScannerScreen(
            onDismiss = { showScanner = false },
            onScanComplete = { data ->
                vm.setScannedData(data)
                showScanner = false
                showDialog = true
            }
        )
    }
}

// DonationEntryDialog remains the same.
@Composable
fun DonationEntryDialog(
    scannedData: ScannedCheckData?,
    batchId: Long,
    batchDate: Long,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Long, String?, Long) -> Unit
) {
    var firstName by remember { mutableStateOf(scannedData?.firstName ?: "") }
    var lastName by remember { mutableStateOf(scannedData?.lastName ?: "") }
    var checkNumber by remember { mutableStateOf(scannedData?.checkNumber ?: "") }
    var amount by remember { mutableStateOf(scannedData?.amount ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Donation") },
        text = {
            Column {
                OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(checkNumber, { checkNumber = it }, label = { Text("Check Number") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") })
            }
        },
        confirmButton = {
            Button(
                enabled = batchId > 0L,
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        onSave(firstName, lastName, checkNumber, amt, batchDate, scannedData?.imageData, batchId)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
