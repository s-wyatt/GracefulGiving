package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.util.printDepositSlip

@Composable
fun BatchEntryScreen(
    batchId: Long,
    vm: BatchEntryViewModel = hiltViewModel()
) {

    val state by vm.uiState.collectAsState()
    var showAddDonationDialog by remember { mutableStateOf(false) }
    var showDeleteDonationDialog by remember { mutableStateOf<DonationEntity?>(null) }
    var showEditDonationDialog by remember { mutableStateOf<DonationEntity?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var showDepositSlip by remember { mutableStateOf(false) }
    var showCloseConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(batchId) {
        vm.loadBatch(batchId)
    }

    val batchWithDonations = state.batchWithDonations
    val donations = batchWithDonations?.donations ?: emptyList()
    val donationsCount = donations.size
    val totalAmount = donations.sumOf { it.donation.checkAmount }
    val totalAmountText = "$${"%.2f".format(totalAmount)}"
    val isBatchClosed = batchWithDonations?.batch?.status == "closed"

    Scaffold(
        floatingActionButton = {
            if (!isBatchClosed) {
                FloatingActionButton(onClick = { showAddDonationDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Donation")
                }
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
                    if (isBatchClosed) {
                        Text("Closed", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Scan Check Button
                Button(
                    onClick = { showScanner = true },
                    enabled = !isBatchClosed,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan Check")
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Check")
                }

                // Deposit Slip Button
                Button(
                    onClick = { showDepositSlip = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Print Deposit Slip")
                }
            }

            // Close Batch Button
            Button(
                onClick = { showCloseConfirmation = true },
                enabled = !isBatchClosed,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Close Batch")
            }

            // Donations List
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(donations, key = { it.donation.donationId }) { donationWithDonor ->
                    val donorName = "${donationWithDonor.donor.firstName} ${donationWithDonor.donor.lastName}"
                    val donationText = "Check #${donationWithDonor.donation.checkNumber} - $${"%.2f".format(donationWithDonor.donation.checkAmount)}"

                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(donorName, style = MaterialTheme.typography.titleMedium)
                                Text(donationText)
                            }
                            if (!isBatchClosed) {
                                IconButton(onClick = { showEditDonationDialog = donationWithDonor.donation }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Donation")
                                }
                                IconButton(onClick = { showDeleteDonationDialog = donationWithDonor.donation }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Donation")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDonationDialog) {
        DonationEntryDialog(
            scannedData = state.scannedData,
            batchId = batchWithDonations?.batch?.batchId ?: 0L,
            batchDate = batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis(),
            onDismiss = {
                showAddDonationDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img, bId ->
                vm.addDonation(fn, ln, cn, amt, dt, img, bId)
                showAddDonationDialog = false
                vm.clearScannedData()
            }
        )
    }

    showEditDonationDialog?.let {
        DonationEntryDialog(
            donation = it,
            batchId = batchWithDonations?.batch?.batchId ?: 0L,
            batchDate = batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis(),
            onDismiss = { showEditDonationDialog = null },
            onSave = { _, _, _, _, _, _, _ ->
                vm.updateDonation(it)
                showEditDonationDialog = null
            }
        )
    }

    showDeleteDonationDialog?.let {
        AlertDialog(
            onDismissRequest = { showDeleteDonationDialog = null },
            title = { Text("Delete Donation?") },
            text = { Text("Are you sure you want to delete this donation?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteDonation(it.donationId)
                        showDeleteDonationDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDonationDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showScanner) {
        CheckScannerScreen(
            onDismiss = { showScanner = false },
            onScanComplete = { data ->
                vm.setScannedData(data)
                showScanner = false
                showAddDonationDialog = true
            }
        )
    }

    if (showDepositSlip) {
        DepositSlipDialog(
            bankSettings = state.bankSettings,
            donations = donations,
            batchDate = batchWithDonations?.batch?.createdOn ?: 0L,
            onDismiss = { showDepositSlip = false },
            onPrint = {
                printDepositSlip(context, state.bankSettings, donations, batchWithDonations?.batch?.createdOn ?: 0L)
                showDepositSlip = false
                showCloseConfirmation = true
            }
        )
    }

    if (showCloseConfirmation) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmation = false },
            title = { Text("Close Batch?") },
            text = { Text("Would you like to close this batch?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.closeBatch(batchId)
                        showCloseConfirmation = false
                    }
                ) {
                    Text("Close Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DepositSlipDialog(
    bankSettings: BankSettingsEntity?,
    donations: List<DonationWithDonor>,
    batchDate: Long,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit Slip") },
        text = {
            Column {
                bankSettings?.let {
                    Text("Bank Name: ${it.bankName}")
                    Text("Account Name: ${it.accountName}")
                    Text("Account Number: ${it.accountNumber}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Donations:")
                donations.forEach {
                    Text("${it.donor.firstName} ${it.donor.lastName} - #${it.donation.checkNumber} - $${"%.2f".format(it.donation.checkAmount)}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Total Checks: ${donations.size}")
                Text("Total Amount: $${"%.2f".format(donations.sumOf { it.donation.checkAmount })}")
            }
        },
        confirmButton = {
            Button(onClick = onPrint) {
                Text("Print")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DonationEntryDialog(
    scannedData: ScannedCheckData? = null,
    donation: DonationEntity? = null,
    batchId: Long,
    batchDate: Long,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Long, String?, Long) -> Unit
) {
    var firstName by remember { mutableStateOf(donation?.let { "" } ?: scannedData?.firstName ?: "") }
    var lastName by remember { mutableStateOf(donation?.let { "" } ?: scannedData?.lastName ?: "") }
    var checkNumber by remember { mutableStateOf(donation?.checkNumber ?: scannedData?.checkNumber ?: "") }
    var amount by remember { mutableStateOf(donation?.checkAmount?.toString() ?: scannedData?.amount ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (donation == null) "Add Donation" else "Edit Donation") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName, 
                    onValueChange = { firstName = it }, 
                    label = { Text("First Name") }, 
                    enabled = donation == null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName, 
                    onValueChange = { lastName = it }, 
                    label = { Text("Last Name") }, 
                    enabled = donation == null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = checkNumber, 
                    onValueChange = { checkNumber = it }, 
                    label = { Text("Check Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = batchId > 0L,
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        if (donation == null) {
                            onSave(firstName, lastName, checkNumber, amt, batchDate, scannedData?.imageData, batchId)
                        } else {
                            onSave(firstName, lastName, checkNumber, amt, batchDate, null, batchId)
                        }
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
