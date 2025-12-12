package com.gracechurch.gracefulgiving.ui.batch

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchManagementScreen(
    // GENTLE FIX: Simplified parameters to only take NavController.
    // The MainScaffold now handles the back button.
    navController: NavController,
    vm: BatchManagementViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val batches = state.filteredAndSorted

    // Assuming you'll get the userId from a shared ViewModel or similar in a real app.
    // For now, let's hardcode it for the createNewBatch function.
    val currentUserId = 1L // TODO: Replace with actual logged-in user ID

    LaunchedEffect(Unit) { vm.loadBatches() }

    Scaffold(
        // The TopAppBar is now managed by MainScaffold, so we remove it from here.
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Create a new batch and then navigate to the entry screen
                    vm.createNewBatch(currentUserId) { newBatchId ->
                        navController.navigate("${Routes.BATCH_ENTRY}/$newBatchId")
                    }
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
            // Add sorting/filtering UI here, as it's no longer in the TopAppBar
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
                ) {
                    items(batches, key = { it.batch.batchId }) { batchWithDonations ->
                        SwipeToDeleteContainer(
                            onDelete = { vm.deleteBatch(batchWithDonations.batch.batchId) }
                        ) {
                            BatchCard(
                                batchWithDonations = batchWithDonations,
                                onEdit = {
                                    // Navigate to the specific batch for editing
                                    navController.navigate("${Routes.BATCH_ENTRY}/${batchWithDonations.batch.batchId}")
                                },
                                onPrint = { vm.printBatchReport(batchWithDonations.batch.batchId) },
                                onDepositSlip = { vm.printDepositSlip(batchWithDonations.batch.batchId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchCard(
    batchWithDonations: ERROR,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onDepositSlip: () -> Unit
) {
    TODO("Not yet implemented")
}

// (The rest of the file remains the same: SortingMenu, FilteringMenu, EmptyState, etc.)
// ...
