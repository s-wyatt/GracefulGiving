package com.gracechurch.gracefulgiving.ui.donation

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import base64ToBitmap
import com.gracechurch.gracefulgiving.domain.model.Donor
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
            viewModel.loadAllDonors()
        }
    }

    val donationState by viewModel.donation.collectAsState()
    val currentDonor by viewModel.currentDonor.collectAsState()
    val allDonors by viewModel.donors.collectAsState()
    val context = LocalContext.current

    Log.d("DonationScreen", "Recomposing. Donation state: ${donationState?.donationId}, Image non-null: ${donationState?.checkImage != null}")

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
            var firstName by remember(currentDonor) { mutableStateOf(currentDonor?.firstName ?: "") }
            var lastName by remember(currentDonor) { mutableStateOf(currentDonor?.lastName ?: "") }
            var checkNumber by remember(donation) { mutableStateOf(donation.checkNumber) }
            var checkAmount by remember(donation) { mutableStateOf(donation.checkAmount.toString()) }
            val checkDate = remember(donation) {
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(donation.checkDate))
            }
            var showFullScreenImage by remember { mutableStateOf(false) }
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            var bitmapError by remember { mutableStateOf<Exception?>(null) }
            var isLoadingBitmap by remember { mutableStateOf(false) }
            
            var selectedAliasDonor by remember { mutableStateOf<Donor?>(null) }
            var aliasDropdownExpanded by remember { mutableStateOf(false) }

            LaunchedEffect(donation.checkImage) {
                if (!donation.checkImage.isNullOrEmpty()) {
                    isLoadingBitmap = true
                    bitmapError = null
                    try {
                        bitmap = base64ToBitmap(donation.checkImage)
                        Log.d("DonationScreen", "Bitmap decoding successful")
                    } catch (e: Exception) {
                        Log.e("DonationScreen", "Error decoding bitmap from Base64", e)
                        bitmapError = e
                    }
                    isLoadingBitmap = false
                } else {
                    bitmap = null
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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

                ExposedDropdownMenuBox(
                    expanded = aliasDropdownExpanded,
                    onExpandedChange = { aliasDropdownExpanded = !aliasDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAliasDonor?.let { "${it.firstName} ${it.lastName}" } ?: "No Alias",
                        onValueChange = { },
                        label = { Text("Assign to Donor (Alias)") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aliasDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = aliasDropdownExpanded,
                        onDismissRequest = { aliasDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Alias") },
                            onClick = {
                                selectedAliasDonor = null
                                aliasDropdownExpanded = false
                            }
                        )
                        allDonors.forEach { donor ->
                            DropdownMenuItem(
                                text = { Text("${donor.firstName} ${donor.lastName}") },
                                onClick = {
                                    selectedAliasDonor = donor
                                    aliasDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val updatedDonation = donation.copy(
                                checkNumber = checkNumber,
                                checkAmount = checkAmount.toDoubleOrNull() ?: donation.checkAmount,
                                donorId = selectedAliasDonor?.donorId ?: donation.donorId
                            )
                            viewModel.updateDonation(updatedDonation)
                            Toast.makeText(context, "Donation Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedAliasDonor?.let {
                                viewModel.addAlias(it.donorId, firstName, lastName)
                                val updatedDonation = donation.copy(donorId = it.donorId)
                                viewModel.updateDonation(updatedDonation)
                                Toast.makeText(context, "Alias created and donation reassigned", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = selectedAliasDonor != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Alias")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoadingBitmap -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Loading image...", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Check Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clickable { showFullScreenImage = true },
                            contentScale = ContentScale.Fit
                        )
                    }
                    bitmapError != null -> {
                        Text("Error loading image: ${bitmapError?.message}", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    donation.checkImage.isNullOrEmpty() -> {
                        Text("No image available.", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    else -> {
                        Text("Unknown image state", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }

            if (showFullScreenImage && bitmap != null) {
                FullScreenImageDialog(
                    bitmap = bitmap!!,
                    onDismiss = { showFullScreenImage = false }
                )
            }
        }
    }
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
