package com.gracechurch.gracefulgiving.ui.donor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.ui.donation.DonationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDetailScreen(
    donorId: Long,
    navController: NavController,
    viewModel: DonorDetailViewModel = hiltViewModel(),
    donationViewModel: DonationViewModel = hiltViewModel()
) {
    val donorState by viewModel.donor.collectAsState()
    val donationsState by viewModel.donations.collectAsState()
    var sortAscending by remember { mutableStateOf(true) }

    var showCheckImageDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(donorId) {
        viewModel.loadDonor(donorId)
        viewModel.loadDonations(donorId)
    }

    val sortedDonations = if (sortAscending) {
        donationsState.sortedBy { it.donationDate }
    } else {
        donationsState.sortedByDescending { it.donationDate }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donor Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            donorState?.let {
                DonorInfo(donor = it, onSave = { viewModel.updateDonor(it) })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text("Donations")
                IconButton(onClick = { sortAscending = !sortAscending }) {
                    Icon(
                        if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Sort"
                    )
                }
            }

            LazyColumn {
                items(sortedDonations) { donation ->
                    DonationListItem(donation = donation) {
                        donation.checkImagePath?.let { path ->
                            showCheckImageDialog = path
                        }
                    }
                }
            }
        }
    }

    showCheckImageDialog?.let {
        val checkImage by donationViewModel.getCheckImage(it).collectAsState(initial = null)

        AlertDialog(
            onDismissRequest = { showCheckImageDialog = null },
            title = { Text("Check Image") },
            text = {
                checkImage?.let { bitmap ->
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Check Image")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCheckImageDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DonorInfo(donor: Donor, onSave: (Donor) -> Unit) {
    var firstName by remember { mutableStateOf(donor.firstName) }
    var lastName by remember { mutableStateOf(donor.lastName) }
    var address by remember { mutableStateOf(donor.address) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSave(donor.copy(firstName = firstName, lastName = lastName, address = address)) }) {
            Text("Save")
        }
    }
}

@Composable
fun DonationListItem(donation: Donation, onDonationClick: (Donation) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onDonationClick(donation) }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Amount: $${donation.checkAmount}")
                Text("Date: ${donation.donationDate}")
            }
            donation.checkImagePath?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Check Image",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}