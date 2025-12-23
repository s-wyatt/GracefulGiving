package com.gracechurch.gracefulgiving.ui.statements

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.gracechurch.gracefulgiving.domain.model.DonationListItem
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.util.printYearlyStatement
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyStatementsScreen(
    navController: NavController,
    vm: YearlyStatementsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    val sortedDonors = remember(state.donors, state.sortType, state.donationsForSelectedYear) {
        val donationTotals = state.donationsForSelectedYear.groupBy { it.donorId }
            .mapValues { (_, donations) -> donations.sumOf { it.checkAmount } }

        when (state.sortType) {
            SortType.NAME_ASCENDING -> state.donors.sortedWith(compareBy({ it.lastName }, { it.firstName }))
            SortType.TOTAL_DESCENDING -> state.donors.sortedByDescending { donationTotals[it.donorId] ?: 0.0 }
            SortType.TOTAL_ASCENDING -> state.donors.sortedBy { donationTotals[it.donorId] ?: 0.0 }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Yearly Giving Statements") })
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.selectedYear ?: "Select a Year",
                    onValueChange = {},
                    label = { Text("Year") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year) },
                            onClick = {
                                vm.onYearSelected(year)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (state.selectedYear != null) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            TextButton(onClick = { vm.selectAllDonors() }) { Text("Select All") }
                            TextButton(onClick = { vm.clearAllDonors() }) { Text("Clear All") }
                        }
                        val selectedCount = state.selectedDonorIdsForPrint.size
                        Button(
                            onClick = {
                                Toast.makeText(context, "Printing $selectedCount statements...", Toast.LENGTH_SHORT).show()
                            },
                            enabled = selectedCount > 0
                        ) {
                            Text("Print $selectedCount")
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        var sortMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { sortMenuExpanded = true }) {
                                Text("Sort by: ${state.sortType.name.replace('_', ' ').lowercase().capitalize()}")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort Options")
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(text = { Text("Name Ascending") }, onClick = { vm.setSortType(SortType.NAME_ASCENDING); sortMenuExpanded = false })
                                DropdownMenuItem(text = { Text("Total Descending") }, onClick = { vm.setSortType(SortType.TOTAL_DESCENDING); sortMenuExpanded = false })
                                DropdownMenuItem(text = { Text("Total Ascending") }, onClick = { vm.setSortType(SortType.TOTAL_ASCENDING); sortMenuExpanded = false })
                            }
                        }
                        
                        Spacer(Modifier.width(16.dp))

                        if(state.isAdmin) {
                             Button(
                                onClick = vm::onMergeClicked,
                                enabled = state.selectedDonorIdsForPrint.size == 2
                            ) {
                                Text("Merge Donors")
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedDonors, key = { it.donorId }) { donor ->
                        val donationsForYear = state.donationsForSelectedYear.filter { it.donorId == donor.donorId }
                        val totalAmount = donationsForYear.sumOf { it.checkAmount }
                        val isSelected = state.selectedDonorIdsForPrint.contains(donor.donorId)

                        Card(Modifier.fillMaxWidth().clickable(enabled = state.selectedYear != null) { vm.onDonorSelected(donor.donorId) }) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                if (state.selectedYear != null) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { vm.toggleDonorSelection(donor.donorId) }
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text("${donor.firstName} ${donor.lastName}", style = MaterialTheme.typography.titleMedium)
                                    if (state.selectedYear != null) {
                                        Text("Donations: ${donationsForYear.size}")
                                        Text("Total for ${state.selectedYear}: $${"%.2f".format(totalAmount)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showMergeDialog) {
        val donorsToMerge = state.donors.filter { state.selectedDonorIdsForPrint.contains(it.donorId) }
        MergeDonorsDialog(
            donors = donorsToMerge,
            onDismiss = vm::onDismissMergeDialog,
            onConfirm = vm::mergeDonors
        )
    }

    if (state.selectedDonorDonations.isNotEmpty()) {
        val selectedDonor = state.donors.find { it.donorId == state.selectedDonorId }
        val donorName = selectedDonor?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Donor"

        StatementPreviewDialog(
            donations = state.selectedDonorDonations,
            donorName = donorName,
            year = state.selectedYear ?: "N/A",
            onDismiss = { vm.onDonorSelected(null) },
            onPrint = {
                val file = printYearlyStatement(context, donorName, state.selectedDonorDonations, state.selectedYear ?: "N/A")
                openPdf(context, file)
            }
        )
    }
}

@Composable
fun MergeDonorsDialog(donors: List<Donor>, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var selectedDestination by remember { mutableStateOf<Donor?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Merge Donors") },
        text = {
            Column {
                Text("Select which donor record to keep. All donations from the other donor will be moved to this one.")
                Spacer(Modifier.height(16.dp))
                donors.forEach { donor ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedDestination?.donorId == donor.donorId,
                            onClick = { selectedDestination = donor }
                        )
                        Text("${donor.firstName} ${donor.lastName}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDestination?.let { onConfirm(it.donorId) } },
                enabled = selectedDestination != null
            ) { Text("Confirm Merge") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StatementPreviewDialog(donations: List<DonationListItem>, donorName: String, year: String, onDismiss: () -> Unit, onPrint: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Statement Preview - $donorName ($year)") },
        text = {
            LazyColumn {
                item {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Date", Modifier.weight(1f))
                        Text("Check #", Modifier.weight(1f))
                        Text("Amount", Modifier.weight(1f))
                    }
                }
                items(donations.sortedBy { it.checkDate }) { donation ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(dateFormat.format(Date(donation.checkDate)), Modifier.weight(1f))
                        Text(donation.checkNumber, Modifier.weight(1f))
                        Text("$${"%.2f".format(donation.checkAmount)}", Modifier.weight(1f))
                    }
                }
                item {
                    val totalAmount = donations.sumOf { it.checkAmount }
                    Text("Total Donations: ${donations.size}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                    Text("Total Amount: $${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = { Button(onClick = onPrint) { Text("Print") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
    }
    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}
