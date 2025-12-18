package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import com.gracechurch.gracefulgiving.data.mappers.toDomain
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.util.openPdf
import com.gracechurch.gracefulgiving.util.printDepositReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEntryScreen(
    batchId: Long,
    onBack: () -> Unit,
    vm: BatchEntryViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    var showAddDonationDialog by remember { mutableStateOf(false) }
    var showDeleteDonationDialog by remember { mutableStateOf<Donation?>(null) }
    var showEditDonationDialog by remember { mutableStateOf<Donation?>(null) }
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
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Batch Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isBatchClosed) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Scan check", modifier = Modifier.padding(end = 8.dp))
                        IconButton(
                            onClick = { showScanner = true },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Scan Check",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add a donation manually", modifier = Modifier.padding(end = 8.dp))
                        FloatingActionButton(onClick = { showAddDonationDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Donation")
                        }
                    }
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

            // Deposit Slip and Close Batch Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Deposit Slip Button
                Button(
                    onClick = { showDepositSlip = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Print Deposit Slip")
                }
                // Close Batch Button
                Button(
                    onClick = { showCloseConfirmation = true },
                    enabled = !isBatchClosed,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close Batch")
                }
            }

            // Donations List
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(donations, key = { it.donation.donationId }) { donationWithDonor ->
                    val donorName = "${donationWithDonor.donor.firstName} ${donationWithDonor.donor.lastName}"
                    val donationText = if(donationWithDonor.donation.checkNumber == "Cash") {
                        "Cash - $${"%.2f".format(donationWithDonor.donation.checkAmount)}"
                    } else {
                        "Check #${donationWithDonor.donation.checkNumber} - $${"%.2f".format(donationWithDonor.donation.checkAmount)}"
                    }

                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(donorName, style = MaterialTheme.typography.titleMedium)
                                Text(donationText)
                            }
                            if (!isBatchClosed) {
                                IconButton(onClick = { showEditDonationDialog = donationWithDonor.donation.toDomain() }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Donation")
                                }
                                IconButton(onClick = { showDeleteDonationDialog = donationWithDonor.donation.toDomain() }) {
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
            onSave = { _, _, updatedCheckNumber, updatedAmount, _, _ ->
                val updatedDonation = existingDonation.copy(
                    checkNumber = updatedCheckNumber,
                    checkAmount = updatedAmount
                )
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
                    val file = printDepositReport(context, fund, donations, batchWithDonations?.batch?.createdOn ?: 0L)
                    openPdf(context, file)
                    showDepositSlip = false
                    showCloseConfirmation = true
                } catch (e: Exception) {
                    // Handle error
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
    donation: Donation? = null,
    batchId: Long,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, checkNumber: String, amount: Double, date: Long, image: String?) -> Unit
) {
    var firstName by remember { mutableStateOf(scannedData?.firstName ?: "") }
    var lastName by remember { mutableStateOf(scannedData?.lastName ?: "") }
    var checkNumber by remember { mutableStateOf(donation?.checkNumber ?: scannedData?.checkNumber ?: "") }
    var amount by remember { mutableStateOf(donation?.checkAmount?.toString() ?: scannedData?.amount ?: "") }
    var isCash by remember { mutableStateOf(donation?.checkNumber == "Cash") }

    val dialogTextStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.7f)

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
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = dialogTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = dialogTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isCash, onCheckedChange = { isCash = it })
                    Text("Cash", style = dialogTextStyle, modifier = Modifier.padding(end = 16.dp))

                    OutlinedTextField(
                        value = if (isCash) "Cash" else checkNumber,
                        onValueChange = { if (!isCash) checkNumber = it },
                        label = { Text("Check Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isCash,
                        textStyle = dialogTextStyle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = dialogTextStyle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = (donation != null || (firstName.isNotBlank() && lastName.isNotBlank())) &&
                        (isCash || checkNumber.isNotBlank()) && amount.toDoubleOrNull() != null && batchId > 0L,
                onClick = {
                    amount.toDoubleOrNull()?.let { amt ->
                        onSave(firstName, lastName, if (isCash) "Cash" else checkNumber, amt, System.currentTimeMillis(), scannedData?.imageData)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
