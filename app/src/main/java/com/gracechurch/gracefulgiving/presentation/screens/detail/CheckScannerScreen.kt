package com.gracechurch.gracefulgiving.presentation.screens.detail
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckScannerScreen(onDismiss: () -> Unit, onScanComplete: (ScannedCheckData) -> Unit) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Scan Check") }, navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") } }) }) { pad ->
        if (!hasPermission) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Camera permission required")
            }
        } else {
            Box(Modifier.fillMaxSize().padding(pad)) {
                AndroidView(factory = { PreviewView(it).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycle, cameraSelector, preview)
                        } catch (e: Exception) { }
                    }, ContextCompat.getMainExecutor(ctx))
                } }, modifier = Modifier.fillMaxSize())

                if (isProcessing) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                FloatingActionButton(
                    onClick = {
                        isProcessing = true
                        val mockData = ScannedCheckData(
                            firstName = "John",
                            lastName = "Doe",
                            checkNumber = (1000..9999).random().toString(),
                            amount = "100.00",
                            imageData = "base64mockimage"
                        )
                        onScanComplete(mockData)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, "Capture")
                }
            }
        }
    }
}
