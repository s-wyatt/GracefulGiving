package com.gracechurch.gracefulgiving.presentation.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.presentation.viewmodel.BatchEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEntryScreen(vm: BatchEntryViewModel = hiltViewModel(), userId: Long) {

    val state by vm.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    // Create batch once
    LaunchedEffect(Unit) {
        if (state.batchWithDonations == null) {
            vm.createBatch(System.currentTimeMillis(), userId)
        }
    }

    // Current batch references
    val batchWithDonations = state.batchWithDonations
    val batchNumberText = batchWithDonations?.batch?.batchNumber?.toString() ?: ""
    val donations = batchWithDonations?.donations ?: emptyList()
    val donationsCount = donations.size
    val totalAmount = donations.sumOf { it.donation.checkAmount }
    val totalAmountText = "$${"%.2f".format(totalAmount)}"

    Scaffold(
        topBar = { TopAppBar(title = { Text("Batch Entry: $batchNumberText") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
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
                items(donations) { donationWithDonor ->

                    val donorName =
                        "${donationWithDonor.donor.firstName} ${donationWithDonor.donor.lastName}"

                    val donationText =
                        "Check #${donationWithDonor.donation.checkNumber} - $${"%.2f".format(donationWithDonor.donation.checkAmount)}"

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

    // Donation entry dialog
    if (showDialog) {
        DonationEntryDialog(
            scannedData = state.scannedData,
            batchDate = batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis(),
            onDismiss = {
                showDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img ->
                vm.addDonation(fn, ln, cn, amt, dt, img)
                showDialog = false
                vm.clearScannedData()
            }
        )
    }

    // Check scanner UI
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

@Composable
fun DonationEntryDialog(
    scannedData: ScannedCheckData?,
    batchDate: Long,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Long, String?) -> Unit
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
            Button(onClick = {
                amount.toDoubleOrNull()?.let { amt ->
                    onSave(firstName, lastName, checkNumber, amt, batchDate, scannedData?.imageData)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}