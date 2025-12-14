package com.gracechurch.gracefulgiving.ui.batch

import android.Manifest
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.util.CheckImageAnalyzer
import com.gracechurch.gracefulgiving.util.openPdf
import com.gracechurch.gracefulgiving.util.printDepositSlip
import java.util.concurrent.Executors

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
    val fund = state.fund

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
                    fund?.let {
                        Column {
                            Text("Fund", style = MaterialTheme.typography.bodySmall)
                            Text(it.name, style = MaterialTheme.typography.headlineMedium)
                        }
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
            batchId = batchId,
            onDismiss = {
                showAddDonationDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img ->
                vm.addDonation(fn, ln, cn, amt, dt, img, batchId)
                showAddDonationDialog = false
                vm.clearScannedData()
            }
        )
    }

    showEditDonationDialog?.let { existingDonation ->
        DonationEntryDialog(
            donation = existingDonation,
            batchId = batchId,
            onDismiss = { showEditDonationDialog = null },
            // The onSave lambda provides all the fields from the dialog
            onSave = { _, _, updatedCheckNumber, updatedAmount, _, _ ->
                // Construct the updated entity using the values from the dialog
                val updatedDonation = existingDonation.copy(
                    checkNumber = updatedCheckNumber,
                    checkAmount = updatedAmount
                )
                // Now, call the ViewModel function with the correctly typed object
                vm.updateDonation(updatedDonation)
                showEditDonationDialog = null
            }
        )
    }

    showDeleteDonationDialog?.let { donation ->
        AlertDialog(
            onDismissRequest = { showDeleteDonationDialog = null },
            title = { Text("Delete Donation?") },
            text = { Text("Are you sure you want to delete this donation? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteDonation(donation.donationId)
                        showDeleteDonationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDonationDialog = null }) { Text("Cancel") }
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
            fund = fund,
            donations = donations,
            batchDate = batchWithDonations?.batch?.createdOn ?: 0L,
            onDismiss = { showDepositSlip = false },
            onPrint = {
                try {
                    val file = printDepositSlip(context, fund, donations, batchWithDonations?.batch?.createdOn ?: 0L)
                    openPdf(context, file) // Assuming you have an openPdf util function
                    showDepositSlip = false
                    showCloseConfirmation = true
                } catch (e: Exception) {
                    // Handle error (e.g., show a toast)
                }
            }
        )
    }

    if (showCloseConfirmation) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmation = false },
            title = { Text("Close Batch?") },
            text = { Text("Closing a batch is final and prevents further edits. Are you sure you want to continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.closeBatch(batchId)
                        showCloseConfirmation = false
                    }
                ) { Text("Close Batch") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DepositSlipDialog(
    fund: Fund?,
    donations: List<DonationWithDonor>,
    batchDate: Long,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    if (fund == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Fund Information Missing") },
            text = { Text("Cannot print deposit slip because the fund information is not available.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Print Deposit Slip?") },
        text = {
            Column {
                Text("Bank: ${fund.bankName}", style = MaterialTheme.typography.bodyLarge)
                Text("Account: ${fund.accountNumber}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("This will generate a PDF containing ${donations.size} checks totaling $${"%.2f".format(donations.sumOf { it.donation.checkAmount })}.")
            }
        },
        confirmButton = { Button(onClick = onPrint) { Text("Print") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DonationEntryDialog(
    scannedData: ScannedCheckData? = null,
    donation: DonationEntity? = null,
    batchId: Long,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, checkNumber: String, amount: Double, date: Long, image: String?) -> Unit
) {
    var firstName by remember { mutableStateOf(scannedData?.firstName ?: "") }
    var lastName by remember { mutableStateOf(scannedData?.lastName ?: "") }
    var checkNumber by remember { mutableStateOf(donation?.checkNumber ?: scannedData?.checkNumber ?: "") }
    var amount by remember { mutableStateOf(donation?.checkAmount?.toString() ?: scannedData?.amount ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (donation == null) "Add Donation" else "Edit Donation") },
        text = {
            Column {
                if (donation == null) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
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
                enabled = (donation != null || (firstName.isNotBlank() && lastName.isNotBlank())) &&
                        checkNumber.isNotBlank() && amount.toDoubleOrNull() != null && batchId > 0L,
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        onSave(firstName, lastName, checkNumber, amt, System.currentTimeMillis(), scannedData?.imageData)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


