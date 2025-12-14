package com.gracechurch.gracefulgiving.ui.batch

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.util.printDepositSlip
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchManagementScreen(
    navController: NavController,
    userId: Long,
    vm: BatchManagementViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val batches = state.filteredAndSorted
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) { vm.loadBatches() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDatePicker = true }
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                SortingMenu(state.sortType) { vm.setSortType(it) }
                FilteringMenu(state.filterType) { vm.setFilterType(it) }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (batches.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {                    items(batches, key = { it.batch.batchId }) { batchWithDonations ->
                        SwipeToDeleteContainer(
                            onDelete = { vm.deleteBatch(batchWithDonations.batch.batchId, userId) }
                        ) {
                            BatchCard(
                                batchWithDonations = batchWithDonations,
                                onEdit = {
                                    if (batchWithDonations.batch.status == "open") {
                                        navController.navigate("${Routes.BATCH_ENTRY}/${batchWithDonations.batch.batchId}")
                                    }
                                },
                                onPrint = { 
                                    val file = printDepositSlip(context, null, batchWithDonations.donations, batchWithDonations.batch.createdOn)
                                    openPdf(context, file)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showDatePicker = false
                    vm.createNewBatch(userId, datePickerState.selectedDateMillis ?: System.currentTimeMillis()) { newBatchId ->
                        navController.navigate("${Routes.BATCH_ENTRY}/$newBatchId")
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = uri
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun BatchCard(
    batchWithDonations: BatchWithDonations,
    onEdit: () -> Unit,
    onPrint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
                    .padding(end = 8.dp)
            ) {
                Text("Batch #${batchWithDonations.batch.batchNumber}")
                Text("Donations: ${batchWithDonations.donations.size}")
                Text("Total: $${"%.2f".format(batchWithDonations.donations.sumOf { it.donation.checkAmount })}")
            }

            Button(onClick = onPrint) {
                Text("Print")
            }
        }
    }
}
@Composable
fun SortingMenu(sortType: SortType, setSortType: (SortType) -> Unit) {

}

@Composable
fun FilteringMenu(filterType: FilterType, setFilterType: (FilterType) -> Unit) {

}

@Composable
fun EmptyState() {

}

@Composable
fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    content()
}
