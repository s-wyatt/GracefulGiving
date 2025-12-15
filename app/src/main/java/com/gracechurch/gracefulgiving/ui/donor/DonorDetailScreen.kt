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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDetailScreen(
    donorId: Long,
    navController: NavController,
    viewModel: DonorDetailViewModel = hiltViewModel()
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
        donationsState.sortedBy { it.checkDate }
    } else {
        donationsState.sortedByDescending { it.checkDate }
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            donorState?.let {
                DonorInfo(donor = it, onSave = { viewModel.updateDonor(it) })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Donations", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { sortAscending = !sortAscending }) {
                    Icon(
                        if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Sort"
                    )
                }
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(sortedDonations) { donation ->
                    DonationListItem(
                        donation = donation,
                        onDonationClick = {
                            navController.navigate("donation/${donation.donationId}")
                        },
                        onImageClick = { imagePath ->
                            showCheckImageDialog = imagePath
                        }
                    )
                }
            }
        }
    }

    // Dialog to show the check image
    showCheckImageDialog?.let { imagePath ->
        AlertDialog(
            onDismissRequest = { showCheckImageDialog = null },
            title = { Text("Check Image") },
            text = {
                Image(
                    painter = rememberAsyncImagePainter(imagePath),
                    contentDescription = "Check Image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
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
    var firstName by remember(donor.firstName) { mutableStateOf(donor.firstName) }
    var lastName by remember(donor.lastName) { mutableStateOf(donor.lastName) }

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
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSave(donor.copy(firstName = firstName, lastName = lastName)) }) {
            Text("Save")
        }
    }
}

@Composable
fun DonationListItem(
    donation: Donation,
    onDonationClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDonationClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Amount: $${donation.checkAmount}")
                Text("Date: ${donation.checkDate}")
            }
            donation.checkImage?.let { imagePath ->
                Image(
                    painter = rememberAsyncImagePainter(imagePath),
                    contentDescription = "Check Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { onImageClick(imagePath) }
                )
            }
        }
    }
}
