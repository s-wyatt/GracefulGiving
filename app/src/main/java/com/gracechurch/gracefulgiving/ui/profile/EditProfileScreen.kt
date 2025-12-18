package com.gracechurch.gracefulgiving.ui.profile

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.gracechurch.gracefulgiving.ui.components.UserAvatar
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onProfileUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = UCropContract()
    ) { result ->
        when (result) {
            is UCropResult.Success -> {
                Log.d("GracefulGiving_DEBUG", "Crop success: ${result.uri}")
                viewModel.onAvatarUriChanged(result.uri.toString())
            }
            is UCropResult.Error -> {
                Log.e("GracefulGiving_DEBUG", "Crop failed", result.error)
                val errorMsg = result.error.message ?: "Unknown error"
                if (errorMsg.contains("Bitmap could not be decoded")) {
                    Toast.makeText(context, "Could not read image. Source app blocks access.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Crop failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
            UCropResult.Cancelled -> {
                Log.d("GracefulGiving_DEBUG", "Crop cancelled by user")
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { sourceUri ->
                    // Set the avatar URI immediately after taking picture, without cropping
                    Log.d("GracefulGiving_DEBUG", "Picture taken: $sourceUri")
                    viewModel.onAvatarUriChanged(sourceUri.toString())
                }
            }
        }
    )

    LaunchedEffect(uiState.isProfileUpdated) {
        if (uiState.isProfileUpdated) {
            onProfileUpdated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Row {
                    val maleAvatarUri = "icon:male"
                    val femaleAvatarUri = "icon:female"
                    val initialsAvatarUri = "initials"

                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("male") },
                        isSelected = uiState.avatarUri == maleAvatarUri
                    ) {
                        Icon(
                            Icons.Default.Man,
                            contentDescription = "Male Avatar",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("female") },
                        isSelected = uiState.avatarUri == femaleAvatarUri
                    ) {
                        Icon(
                            Icons.Default.Woman,
                            contentDescription = "Female Avatar",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("initials") },
                        isSelected = uiState.avatarUri == null || uiState.avatarUri == initialsAvatarUri
                    ) {
                        UserAvatar(
                            avatarUri = null,
                            fullName = uiState.fullName,
                            size = 28.8.dp
                        )
                    }
                }
                Row {
                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("dog") },
                        isSelected = uiState.avatarUri == "icon:dog"
                    ) {
                        Text("🐶", style = MaterialTheme.typography.headlineMedium)
                    }
                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("cat") },
                        isSelected = uiState.avatarUri == "icon:cat"
                    ) {
                        Text("🐱", style = MaterialTheme.typography.headlineMedium)
                    }
                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("coffee") },
                        isSelected = uiState.avatarUri == "icon:coffee"
                    ) {
                        Text("☕", style = MaterialTheme.typography.headlineMedium)
                    }
                    AvatarIconButton(
                        onClick = { viewModel.onAvatarChanged("unicorn") },
                        isSelected = uiState.avatarUri == "icon:unicorn"
                    ) {
                        Text("🦄", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Row {
                    IconButton(onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            val newImageUri = context.createTempImageFile()
                            tempImageUri = newImageUri
                            takePictureLauncher.launch(newImageUri)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Take Picture", modifier = Modifier.size(32.dp))
                    }
                    
                    val isDefault = uiState.avatarUri == null || uiState.avatarUri!!.startsWith("icon:") || uiState.avatarUri!!.startsWith("initials:")
                    
                    if (!isDefault) {
                        IconButton(onClick = {
                            try {
                                val sourceUri = Uri.parse(uiState.avatarUri)
                                val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg"))
                                cropImageLauncher.launch(Pair(sourceUri, destinationUri))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "Error: UCrop Activity not found. Check dependencies.", Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            }
                        }) {
                            Icon(
                                Icons.Default.Crop,
                                contentDescription = "Crop Avatar",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Spacing between avatar options and text fields

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = viewModel::onFullNameChanged,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::updateProfile,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.fullName.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save")
            }
        }
    }
}

@Composable
private fun AvatarIconButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = CircleShape
            )
    ) {
        content()
    }
}

private fun Context.createTempImageFile(): Uri {
    val file = File.createTempFile("avatar", ".jpg", externalCacheDir)
    return FileProvider.getUriForFile(
        this,
        "${packageName}.provider",
        file
    )
}

sealed class UCropResult {
    data class Success(val uri: Uri) : UCropResult()
    data class Error(val error: Throwable) : UCropResult()
    object Cancelled : UCropResult()
}

class UCropContract : ActivityResultContract<Pair<Uri, Uri>, UCropResult>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent {
        val options = UCrop.Options().apply {
            setCircleDimmedLayer(true)
            setShowCropGrid(false)
            setToolbarColor(Color(0xFF6750A4).toArgb()) // Use Material Primary color
            setStatusBarColor(Color(0xFF6750A4).toArgb())
            setActiveControlsWidgetColor(Color(0xFF6750A4).toArgb())
            setFreeStyleCropEnabled(true)
        }
        return UCrop.of(input.first, input.second)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): UCropResult {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            val uri = UCrop.getOutput(intent)
            return if (uri != null) UCropResult.Success(uri) else UCropResult.Error(Exception("Uri is null"))
        } else if (resultCode == UCrop.RESULT_ERROR && intent != null) {
            val error = UCrop.getError(intent)
            return UCropResult.Error(error ?: Exception("Unknown uCrop error"))
        }
        return UCropResult.Cancelled
    }
}
