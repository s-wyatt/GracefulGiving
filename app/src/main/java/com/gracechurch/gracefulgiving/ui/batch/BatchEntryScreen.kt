package com.gracechurch.gracefulgiving.ui.batch

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import base64ToBitmap
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import com.gracechurch.gracefulgiving.data.mappers.toDomain
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.util.openPdf
import com.gracechurch.gracefulgiving.util.printDepositReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val funds = state.funds
    val matchedDonor = state.matchedDonor

    // State for editable batch fields
    var batchDate by remember(batchWithDonations) { mutableStateOf(batchWithDonations?.batch?.createdOn ?: System.currentTimeMillis()) }
    var selectedFundId by remember(batchWithDonations) { mutableStateOf(batchWithDonations?.batch?.fundId) }
    var fundDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    val calendar = Calendar.getInstance().apply { timeInMillis = batchDate }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            val newDateCal = Calendar.getInstance().apply {
                set(year, month, day)
            }
            batchDate = newDateCal.timeInMillis
            // Update batch date immediately
             selectedFundId?.let { fid ->
                vm.updateBatchDetails(batchId, batchDate, fid)
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Batch Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            // Editable Batch Details
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Batch Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Date Picker
                        OutlinedTextField(
                            value = dateFormat.format(Date(batchDate)),
                            onValueChange = {},
                            label = { Text("Date") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { if (!isBatchClosed) datePickerDialog.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            },
                            modifier = Modifier.weight(1f).clickable { if (!isBatchClosed) datePickerDialog.show() },
                            enabled = !isBatchClosed
                        )

                        // Fund Dropdown
                        ExposedDropdownMenuBox(
                            expanded = fundDropdownExpanded,
                            onExpandedChange = { if (!isBatchClosed) fundDropdownExpanded = !fundDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val selectedFundName = funds.find { it.fundId == selectedFundId }?.name ?: ""
                            OutlinedTextField(
                                value = selectedFundName,
                                onValueChange = {},
                                label = { Text("Fund") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fundDropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                                enabled = !isBatchClosed
                            )
                            ExposedDropdownMenu(
                                expanded = fundDropdownExpanded,
                                onDismissRequest = { fundDropdownExpanded = false }
                            ) {
                                funds.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f.name) },
                                        onClick = {
                                            selectedFundId = f.fundId
                                            fundDropdownExpanded = false
                                            // Update batch fund immediately
                                            if (f.fundId != null) {
                                                vm.updateBatchDetails(batchId, batchDate, f.fundId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Card
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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

             Spacer(modifier = Modifier.height(16.dp))

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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .then(
                                if (!isBatchClosed) {
                                    Modifier.clickable { showEditDonationDialog = donationWithDonor.donation.toDomain() }
                                } else Modifier
                            )
                    ) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(donorName, style = MaterialTheme.typography.titleMedium)
                                Text(donationText)
                            }
                            if (!isBatchClosed) {
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
            donors = state.donors,
            batchId = batchId,
            matchedDonor = matchedDonor,
            onDismiss = {
                showAddDonationDialog = false
                vm.clearScannedData()
            },
            onSave = { fn, ln, cn, amt, dt, img, donorId ->
                vm.addDonation(fn, ln, cn, amt, dt, img, batchId, donorId)
                showAddDonationDialog = false
                vm.clearScannedData()
            },
            onAddAlias = vm::addAlias
        )
    }

    showEditDonationDialog?.let { existingDonation ->
        DonationEntryDialog(
            donation = existingDonation,
            donors = state.donors,
            batchId = batchId,
            onDismiss = { showEditDonationDialog = null },
            onSave = { _, _, updatedCheckNumber, updatedAmount, _, _, newDonorId ->
                val updatedDonation = existingDonation.copy(
                    checkNumber = updatedCheckNumber,
                    checkAmount = updatedAmount,
                    donorId = newDonorId ?: existingDonation.donorId
                )
                vm.updateDonation(updatedDonation)
                showEditDonationDialog = null
            },
            onAddAlias = vm::addAlias
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
                    Log.e("BatchEntryScreen", "Error printing deposit slip", e)
                    Toast.makeText(context, "Error printing: ${e.message}", Toast.LENGTH_LONG).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationEntryDialog(
    scannedData: ScannedCheckData? = null,
    donation: Donation? = null,
    donors: List<Donor>,
    batchId: Long,
    matchedDonor: Donor? = null,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, checkNumber: String, amount: Double, date: Long, image: String?, donorId: Long?) -> Unit,
    onAddAlias: (donorId: Long, firstName: String, lastName: String) -> Unit
) {
    val existingDonor = remember(donation, donors) {
        if (donation != null) {
            donors.find { it.donorId == donation.donorId }
        } else null
    }

    var firstName by remember { mutableStateOf(existingDonor?.firstName ?: matchedDonor?.firstName ?: scannedData?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existingDonor?.lastName ?: matchedDonor?.lastName ?: scannedData?.lastName ?: "") }
    var checkNumber by remember { mutableStateOf(donation?.checkNumber ?: scannedData?.checkNumber ?: "") }
    var amount by remember { mutableStateOf(donation?.checkAmount?.toString() ?: scannedData?.amount ?: "") }
    var isCash by remember { mutableStateOf(donation?.checkNumber == "Cash") }
    
    var selectedAliasDonor by remember(existingDonor, matchedDonor) { 
        mutableStateOf<Donor?>(existingDonor ?: matchedDonor) 
    }
    var aliasDropdownExpanded by remember { mutableStateOf(false) }
    
    val amountFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    val sortedDonors = remember(donors) { donors.sortedBy { it.lastName } }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    val checkImage = donation?.checkImage ?: scannedData?.imageData
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    LaunchedEffect(checkImage) {
        if (!checkImage.isNullOrEmpty()) {
            try {
                bitmap = base64ToBitmap(checkImage)
            } catch (e: Exception) {
                Log.e("BatchEntryScreen", "Error decoding check image", e)
            }
        }
    }

    if (showFullScreenImage && bitmap != null) {
        FullScreenImageDialog(bitmap = bitmap!!, onDismiss = { showFullScreenImage = false })
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (donation == null) "Add Donation" else "Edit Donation") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = aliasDropdownExpanded,
                        onExpandedChange = { aliasDropdownExpanded = !aliasDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedAliasDonor?.let { "${it.firstName} ${it.lastName}" } ?: "No alias",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aliasDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = aliasDropdownExpanded,
                            onDismissRequest = { aliasDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No alias") },
                                onClick = {
                                    selectedAliasDonor = null
                                    aliasDropdownExpanded = false
                                }
                            )
                            sortedDonors.forEach { donor ->
                                DropdownMenuItem(
                                    text = { Text("${donor.firstName} ${donor.lastName}") },
                                    onClick = {
                                        selectedAliasDonor = donor
                                        firstName = donor.firstName
                                        lastName = donor.lastName
                                        aliasDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isCash, onCheckedChange = { isCash = it })
                    Text("Cash", modifier = Modifier.padding(end = 16.dp))
                    OutlinedTextField(
                        value = if (isCash) "Cash" else checkNumber,
                        onValueChange = { if (!isCash) checkNumber = it },
                        label = { Text("Check Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isCash,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester)
                )
                Spacer(Modifier.height(16.dp))
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Check Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { showFullScreenImage = true },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
             Row {
                Button(
                    onClick = {
                        selectedAliasDonor?.let { 
                            onAddAlias(it.donorId, firstName, lastName)
                            Toast.makeText(context, "Alias created", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedAliasDonor != null
                ) {
                    Text("Alias")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = (donation != null || (firstName.isNotBlank() && lastName.isNotBlank())) &&
                            (isCash || checkNumber.isNotBlank()) && amount.toDoubleOrNull() != null && batchId > 0L,
                    onClick = {
                        amount.toDoubleOrNull()?.let { amt ->
                            onSave(firstName, lastName, if (isCash) "Cash" else checkNumber, amt, System.currentTimeMillis(), scannedData?.imageData, selectedAliasDonor?.donorId)
                        }
                    }
                ) { Text("Save") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun FullScreenImageDialog(bitmap: Bitmap, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                offset += panChange
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Full screen check image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close full screen image")
            }
        }
    }
}
