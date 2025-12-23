package com.gracechurch.gracefulgiving.ui.export

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gracechurch.gracefulgiving.domain.usecase.ExportDonationsUseCase
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDonationsScreen(
    onNavigateUp: () -> Unit,
    exportDonationsUseCase: ExportDonationsUseCase,
    viewModel: ExportDonationsViewModel = viewModel(
        factory = ExportDonationsViewModelFactory(exportDonationsUseCase)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Donations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Exporting donations...")
                } else {
                    Button(
                        onClick = { viewModel.exportDonations() }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Export to CSV")
                    }

                    if (uiState.isSuccess && uiState.filePath != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Export Successful!", color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("File saved to:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            uiState.filePath!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val file = File(uiState.filePath!!)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "text/csv")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open CSV"))
                            }
                        ) {
                            Text("Open File")
                        }
                    }

                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
