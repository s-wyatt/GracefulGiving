package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.Fund
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSelectionScreen(
    vm: BatchSelectionViewModel = hiltViewModel(),
    userId: Long,
    onNavigateToBatchEntry: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val batches = state.batches
    val funds = state.funds

    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var selectedFund by remember { mutableStateOf<Fund?>(null) }
    var expanded by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBatchDialog = true }
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
            if (batches.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No batches yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Create a new batch to get started",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(batches) { batchWithDonations ->
                        val batch = batchWithDonations.batch
                        val donations = batchWithDonations.donations
                        val totalAmount = donations.sumOf { it.donation.checkAmount }
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val dateStr = dateFormat.format(Date(batch.createdOn))

                        Card(
                            Modifier.fillMaxWidth()
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
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            dateStr,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "${donations.size} donations • $${"%.2f".format(totalAmount)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { onNavigateToBatchEntry(batch.batchId) }
                                        ) {
                                            Icon(Icons.Default.Edit, "Continue")
                                        }

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
            title = { Text("Create New Batch") },
            text = {
                Column {
                    Text("Select a fund for the new batch:")
                    Spacer(Modifier.height(16.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedFund?.name ?: "Select a fund",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            funds.forEach { fund ->
                                DropdownMenuItem(
                                    text = { Text(fund.name) },
                                    onClick = {
                                        selectedFund = fund
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFund?.let { fund ->
                            vm.createNewBatch(userId, System.currentTimeMillis(), fund.fundId) { batchId ->
                                onNavigateToBatchEntry(batchId)
                            }
                        }
                        showCreateBatchDialog = false
                    },
                    enabled = selectedFund != null
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(onClick = { showCreateBatchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}