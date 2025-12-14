package com.gracechurch.gracefulgiving.ui.statements

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.domain.model.Donation
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

            // Year selection dropdown
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
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
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

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.donors) { donor ->
                        // Calculate total for this donor for the selected year
                        val donationsForYear = state.selectedDonorDonations.filter { it.donorId == donor.donorId }
                        val totalAmount = donationsForYear.sumOf { it.checkAmount }

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = state.selectedYear != null) { // Only clickable if a year is selected
                                    vm.onDonorSelected(donor.donorId)
                                }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${donor.firstName} ${donor.lastName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                // Show amounts only when a year is selected
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

    // Show the preview dialog only when there are donations for the selected donor
    if (state.selectedDonorDonations.isNotEmpty()) {
        val selectedDonor = state.donors.find { it.donorId == state.selectedDonorId }
        val donorName = selectedDonor?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Donor"

        StatementPreviewDialog(
            donations = state.selectedDonorDonations,
            donorName = donorName,
            year = state.selectedYear ?: "N/A",
            onDismiss = { vm.onDonorSelected(null) }, // Clear selection on dismiss
            onPrint = {
                val file = printYearlyStatement(context, donorName, state.selectedDonorDonations, state.selectedYear ?: "N/A")
                openPdf(context, file)
            }
        )
    }
}

@Composable
fun StatementPreviewDialog(
    donations: List<Donation>,
    donorName: String,
    year: String,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Statement Preview - $donorName ($year)") },
        text = {
            LazyColumn {
                // Header
                item {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Date", Modifier.weight(1f))
                        Text("Check #", Modifier.weight(1f))
                        Text("Amount", Modifier.weight(1f))
                    }
                }
                // List of donations
                items(donations.sortedBy { it.checkDate }) { donation ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(dateFormat.format(Date(donation.checkDate)), Modifier.weight(1f))
                        Text(donation.checkNumber, Modifier.weight(1f))
                        Text("$${"%.2f".format(donation.checkAmount)}", Modifier.weight(1f))
                    }
                }
                // Footer with totals
                item {
                    val totalAmount = donations.sumOf { it.checkAmount }
                    Text(
                        "Total Donations: ${donations.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Total Amount: $${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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

fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/pdf")
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}
