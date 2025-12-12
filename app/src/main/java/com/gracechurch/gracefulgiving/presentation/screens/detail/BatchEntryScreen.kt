package com.gracechurch.gracefulgiving.presentation.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.presentation.viewmodel.BatchEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEntryScreen(
    // CORRECTED PARAMETERS: Receive batchId and onNavigateBack as passed from navigation
    batchId: Long,
    onNavigateBack: () -> Unit,
    vm: BatchEntryViewModel = hiltViewModel()
) {

    val state by vm.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    // CORRECTED LOGIC: Load the batch using the batchId from the navigation argument.
    // The key ensures this effect runs only when batchId changes.
    LaunchedEffect(batchId) {
        if (batchId > 0L) {
            vm.loadBatch(batchId)
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
        topBar = {
            TopAppBar(
                title = { Text("Batch Entry: $batchNumberText") },
                // Add a back button to navigate away from this screen
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
            // CORRECTED: Ensure we have a valid batchId before allowing a donation to be added
            batchId = batchWithDonations?.batch?.batchId ?: 0L,
            batchDate = batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis(),
            onDismiss = {
                showDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img, bId ->
                // Pass the correct batchId when adding the donation
                vm.addDonation(fn, ln, cn, amt, dt, img, bId)
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
    batchId: Long, // Add batchId as a parameter
    batchDate: Long,
    onDismiss: () -> Unit,
    // Update onSave signature to include batchId
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
                // Disable button if batchId is not valid
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
