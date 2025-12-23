package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.Fund
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSelectionScreen(
    vm: BatchSelectionViewModel = hiltViewModel(),
    userId: Long,
    isAdmin: Boolean = false,
    onNavigateToBatchEntry: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val batches = state.batches
    val funds = state.funds

    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<Long?>(null) }
    var selectedFund by remember { mutableStateOf<Fund?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var includeClosedBatches by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }
    var batchDate by remember { mutableStateOf(Date()) }
    var dateText by remember { mutableStateOf(dateFormat.format(batchDate)) }

    val textScale = 1.2f

    LaunchedEffect(Unit) {
        vm.loadBatches()
        vm.loadFunds()
    }

    LaunchedEffect(funds) {
        if (funds.isNotEmpty() && selectedFund == null) {
            selectedFund = funds.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batches") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeClosedBatches, onCheckedChange = { includeClosedBatches = it })
                        Text("Include Closed Batches?")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    batchDate = Date()
                    dateText = dateFormat.format(batchDate)
                    showCreateBatchDialog = true
                }
            ) {
                Icon(Icons.Default.Add, "New Batch")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            val filteredBatches = batches.filter { includeClosedBatches || it.batch.status.lowercase() != "closed" }

            if (filteredBatches.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No batches yet",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize * textScale
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Create a new batch to get started",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBatches) { batchWithDonations ->
                        val batch = batchWithDonations.batch
                        val donations = batchWithDonations.donations
                        val totalAmount = donations.sumOf { it.donation.checkAmount }
                        val fund = funds.find { it.fundId == batch.fundId }
                        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val dateStr = displayDateFormat.format(Date(batch.createdOn))

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBatchEntry(batch.batchId) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Batch #${batch.batchNumber}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = MaterialTheme.typography.titleMedium.fontSize * textScale
                                            )
                                        )
                                        Text(
                                            "$dateStr - ${fund?.name ?: "Unknown Fund"} - ${batch.status}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize * textScale
                                            )
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "${donations.size} donations • $${"%.2f".format(totalAmount)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                                            )
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { vm.printBatchReport(batch.batchId) }
                                        ) {
                                            Icon(Icons.Default.Print, "Print Report")
                                        }

                                        IconButton(
                                            onClick = { vm.printDepositSlip(batch.batchId) }
                                        ) {
                                            Icon(Icons.Default.AccountBalance, "Deposit Slip")
                                        }

                                        if (isAdmin) {
                                            IconButton(
                                                onClick = {
                                                    batchToDelete = batch.batchId
                                                    showDeleteConfirmDialog = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "Delete Batch",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateBatchDialog) {
        AlertDialog(
            onDismissRequest = { showCreateBatchDialog = false },
            title = {
                Text(
                    "Create New Batch",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize * textScale
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = {
                            Text(
                                "Batch Date",
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                            )
                        },
                        textStyle = TextStyle(fontSize = 14.sp * textScale),
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Show Date Picker */ }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))

                    if (funds.size > 1) {
                        Text(
                            "Select a fund for the new batch:",
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                        )
                        Spacer(Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = selectedFund?.name ?: "Select a fund",
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 14.sp * textScale),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                funds.forEach { fund ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                fund.name,
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                                            )
                                        },
                                        onClick = {
                                            selectedFund = fund
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFund?.let { fund ->
                            val fundId = fund.fundId ?: return@let
                            val date = try {
                                dateFormat.parse(dateText)
                            } catch (e: Exception) {
                                Date()
                            }
                            vm.createNewBatch(userId, date.time, fundId) { batchId ->
                                onNavigateToBatchEntry(batchId)
                            }
                        }
                        showCreateBatchDialog = false
                    },
                    enabled = selectedFund != null
                ) {
                    Text(
                        "Create",
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * textScale
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showCreateBatchDialog = false }) {
                    Text(
                        "Cancel",
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * textScale
                    )
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    "Delete Batch",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize * textScale
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this batch? This action cannot be undone.",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        batchToDelete?.let { vm.deleteBatch(it) }
                        showDeleteConfirmDialog = false
                        batchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Delete",
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * textScale
                    )
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmDialog = false
                    batchToDelete = null
                }) {
                    Text(
                        "Cancel",
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * textScale
                    )
                }
            }
        )
    }
}
