package com.gracechurch.gracefulgiving.ui.batch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Base64
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckScannerScreen(onDismiss: () -> Unit, onScanComplete: (ScannedCheckData) -> Unit) {
    val ctx = LocalContext.current
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var permissionState by remember { mutableStateOf<PermissionState>(PermissionState.CHECKING) }
    var isProcessing by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            permissionState = PermissionState.GRANTED
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Check") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        }
    ) { pad ->
        when (permissionState) {
            PermissionState.CHECKING -> {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            PermissionState.DENIED -> {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Camera permission is required to scan checks",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", ctx.packageName, null)
                            }
                            ctx.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    }
                }
            }
            PermissionState.GRANTED -> {
                Box(Modifier.fillMaxSize().padding(pad)) {
                    AndroidView(
                        factory = {
                            PreviewView(it).apply {
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also { preview ->
                                        preview.setSurfaceProvider(surfaceProvider)
                                    }

                                    // Setup ImageCapture
                                    imageCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycle,
                                            cameraSelector,
                                            preview,
                                            imageCapture
                                        )
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isProcessing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Processing check...")
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            imageCapture?.let { capture ->
                                isProcessing = true
                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        @androidx.annotation.OptIn(ExperimentalGetImage::class)
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            processCheckImage(imageProxy) { scannedData ->
                                                isProcessing = false
                                                onScanComplete(scannedData)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isProcessing = false
                                            // Handle error - maybe show a toast
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, "Capture")
                    }
                }
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
private fun processCheckImage(imageProxy: ImageProxy, onResult: (ScannedCheckData) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val fullImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Convert to Base64 for storage
        val bitmap = imageProxy.toBitmap()
        val base64Image = bitmapToBase64(bitmap)

        // Create cropped region for check number (upper right quadrant)
        val upperRightBitmap = cropBitmap(bitmap, 0.5f, 0f, 1f, 0.25f)
        val upperRightImage = InputImage.fromBitmap(upperRightBitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // First, process upper right for check number
        recognizer.process(upperRightImage)
            .addOnSuccessListener { checkNumText ->
                val checkNumber = extractCheckNumber(checkNumText.text)

                // Then process full image for name and amount
                recognizer.process(fullImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val checkData = parseCheckText(text, checkNumber, base64Image)

                        imageProxy.close()
                        onResult(checkData)
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                        // Return with just check number if full image fails
                        onResult(ScannedCheckData(
                            firstName = "",
                            lastName = "",
                            checkNumber = checkNumber,
                            amount = "",
                            imageData = base64Image
                        ))
                    }
            }
            .addOnFailureListener {
                imageProxy.close()
                // Return default data on failure
                onResult(ScannedCheckData(
                    firstName = "",
                    lastName = "",
                    checkNumber = "",
                    amount = "",
                    imageData = base64Image
                ))
            }
    } else {
        imageProxy.close()
        onResult(ScannedCheckData("", "", "", "", ""))
    }
}

/**
 * Crops a bitmap to a specific region
 * @param left Percentage from left (0.0 to 1.0)
 * @param top Percentage from top (0.0 to 1.0)
 * @param right Percentage from left (0.0 to 1.0)
 * @param bottom Percentage from top (0.0 to 1.0)
 */
private fun cropBitmap(bitmap: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val x = (width * left).toInt()
    val y = (height * top).toInt()
    val cropWidth = (width * (right - left)).toInt()
    val cropHeight = (height * (bottom - top)).toInt()

    return Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)
}

/**
 * Extracts check number from upper right region text
 * Looks for 3-6 digit numbers
 */
private fun extractCheckNumber(text: String): String {
    val checkNumRegex = "\\b\\d{3,6}\\b".toRegex()
    return checkNumRegex.find(text)?.value ?: ""
}

/**
 * Parses the full check text for name and amount
 */
private fun parseCheckText(text: String, checkNumber: String, imageData: String): ScannedCheckData {
    val lines = text.lines().filter { it.isNotBlank() }

    var firstName = ""
    var lastName = ""
    var amount = ""

    // Look for dollar amounts
    val amountRegex = "\\$?\\d+\\.\\d{2}".toRegex()
    amount = amountRegex.find(text)?.value?.replace("$", "") ?: ""

    // Try to find name (usually on second line after filtering)
    if (lines.size >= 2) {
        val nameParts = lines[1].trim().split(" ")
        if (nameParts.size >= 2) {
            firstName = nameParts[0]
            lastName = nameParts.drop(1).joinToString(" ")
        }
    }

    return ScannedCheckData(
        firstName = firstName,
        lastName = lastName,
        checkNumber = checkNumber,
        amount = amount,
        imageData = imageData
    )
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@ExperimentalGetImage
private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private enum class PermissionState {
    CHECKING,
    GRANTED,
    DENIED
}
