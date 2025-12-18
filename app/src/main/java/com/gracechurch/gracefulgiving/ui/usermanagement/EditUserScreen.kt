package com.gracechurch.gracefulgiving.ui.usermanagement

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.gracechurch.gracefulgiving.ui.components.UserAvatar
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun EditUserScreen(
    viewModel: EditUserViewModel = hiltViewModel(),
    onUserUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val resultUri = UCrop.getOutput(intent)
                resultUri?.let { viewModel.onAvatarChanged(it.toString()) }
            }
        }
    }

    LaunchedEffect(uiState.isUserUpdated) {
        if (uiState.isUserUpdated) {
            onUserUpdated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit User",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Avatar Display
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.avatarUri?.startsWith("initials:") == true -> {
                    // Display initials avatar
                    val initials = uiState.avatarUri?.removePrefix("initials:") ?: ""
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                uiState.avatarUri?.startsWith("icon:") == true -> {
                    UserAvatar(
                        avatarUri = uiState.avatarUri,
                        fullName = uiState.fullName,
                        size = 100.dp
                    )
                }
                else -> {
                    // Display image from URI or default icon
                    Image(
                        painter = rememberAsyncImagePainter(uiState.avatarUri ?: Icons.Default.Person),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar Selection Buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                // Initials Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("initials") },
                    modifier = Modifier
                        .background(
                            if (uiState.avatarUri?.startsWith("initials:") == true)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    val initials = viewModel.getInitials(uiState.fullName)
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                // Male Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:male") },
                    modifier = Modifier
                        .background(
                            if (uiState.avatarUri == "icon:male")
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Male Avatar")
                }
                // Female Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:female") },
                    modifier = Modifier
                        .background(
                            if (uiState.avatarUri == "icon:female")
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.Face, contentDescription = "Female Avatar")
                }
                // Girl Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:girl") },
                    modifier = Modifier
                        .background(
                            if (uiState.avatarUri == "icon:girl")
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.PersonOutline, contentDescription = "Girl Avatar")
                }
            }
            Row {
                // Dog Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:dog") },
                    modifier = Modifier.background(
                        if (uiState.avatarUri == "icon:dog") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Text("🐶", style = MaterialTheme.typography.titleLarge)
                }
                // Cat Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:cat") },
                    modifier = Modifier.background(
                        if (uiState.avatarUri == "icon:cat") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Text("🐱", style = MaterialTheme.typography.titleLarge)
                }
                // Coffee Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:coffee") },
                    modifier = Modifier.background(
                        if (uiState.avatarUri == "icon:coffee") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Text("☕", style = MaterialTheme.typography.titleLarge)
                }
                // Unicorn Avatar
                IconButton(
                    onClick = { viewModel.onAvatarChanged("icon:unicorn") },
                    modifier = Modifier.background(
                        if (uiState.avatarUri == "icon:unicorn") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = CircleShape
                    )
                ) {
                    Text("🦄", style = MaterialTheme.typography.titleLarge)
                }
            }
            Row {
                IconButton(onClick = { /*TODO: Implement camera*/ }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Picture")
                }
                
                // Crop Button
                if (uiState.isAvatarFile) {
                    IconButton(onClick = { 
                        val sourceUri = Uri.parse(uiState.avatarUri)
                        val destUri = Uri.fromFile(File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
                        val intent = UCrop.of(sourceUri, destUri)
                            .withAspectRatio(1f, 1f)
                            .getIntent(context)
                        cropLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop Image")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = {
                viewModel.onFullNameChanged(it)
                // Update initials avatar if that's currently selected
                if (uiState.avatarUri?.startsWith("initials:") == true) {
                    viewModel.onAvatarChanged("initials")
                }
            },
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.tempPassword,
            onValueChange = viewModel::onTempPasswordChanged,
            label = { Text("Temporary Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /*TODO: Implement password change*/ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Password")
        }

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
            onClick = viewModel::updateUser,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
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
