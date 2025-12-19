package com.gracechurch.gracefulgiving.ui.import

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCase

@Composable
fun ImportDonationsScreen(
    importDonationsUseCase: ImportDonationsUseCase,
    userSessionRepository: UserSessionRepository,
    viewModel: ImportDonationsViewModel = viewModel(
        factory = ImportDonationsViewModelFactory(
            importDonationsUseCase = importDonationsUseCase,
            userSessionRepository = userSessionRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            viewModel.onFileSelected(inputStream)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { launcher.launch("*/*") }) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Select File to Import")
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (uiState.isSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Import Successful!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
