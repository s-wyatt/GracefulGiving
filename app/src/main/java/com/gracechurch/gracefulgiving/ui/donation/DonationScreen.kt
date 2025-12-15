package com.gracechurch.gracefulgiving.ui.donation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    donationId: Long,
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    LaunchedEffect(donationId) {
        if (donationId != 0L) {
            viewModel.loadDonation(donationId)
        }
    }

    val donationState by viewModel.donation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Donation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        donationState?.let { donation ->
            var checkNumber by remember(donation) { mutableStateOf(donation.checkNumber) }
            var checkAmount by remember(donation) { mutableStateOf(donation.checkAmount.toString()) }
            val checkDate = remember(donation) {
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(donation.checkDate))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = checkNumber,
                    onValueChange = { checkNumber = it },
                    label = { Text("Check Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = checkAmount,
                    onValueChange = { checkAmount = it },
                    label = { Text("Check Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = checkDate,
                    onValueChange = { /* Not editable for now */ },
                    label = { Text("Check Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val updatedDonation = donation.copy(
                            checkNumber = checkNumber,
                            checkAmount = checkAmount.toDoubleOrNull() ?: donation.checkAmount
                        )
                        viewModel.updateDonation(updatedDonation)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}