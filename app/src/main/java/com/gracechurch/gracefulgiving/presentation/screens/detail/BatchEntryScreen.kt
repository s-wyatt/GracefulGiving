package com.gracechurch.gracefulgiving.presentation.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEntryScreen(vm: BatchEntryViewModel = hiltViewModel(), userId: Long) {
    val state by vm.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.currentBatch == null) {
            vm.createBatch(System.currentTimeMillis(), userId)
        }
    }

    val batchNumberText = state.currentBatch?.batch?.batchNumber ?: ""
    val donationsCount = state.currentBatch?.donations?.size ?: 0
    val totalAmount = state.currentBatch?.donations?.sumOf { it.checkAmount } ?: 0.0
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
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
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

            Button(
                onClick = { showScanner = true },
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan Check")
                Spacer(Modifier.width(8.dp))
                Text("Scan Check")
            }

            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(state.currentBatch?.donations ?: emptyList()) { donation ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(donation.donorName, style = MaterialTheme.typography.titleMedium)
                            val donationText = "Check #${donation.checkNumber} - $${"%.2f".format(donation.checkAmount)}"
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
            batchDate = state.currentBatch?.batch?.batchDate ?: System.currentTimeMillis(),
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
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = checkNumber,
                    onValueChange = { checkNumber = it },
                    label = { Text("Check Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                amount.toDoubleOrNull()?.let { amt ->
                    onSave(firstName, lastName, checkNumber, amt, batchDate, scannedData?.imageData)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
