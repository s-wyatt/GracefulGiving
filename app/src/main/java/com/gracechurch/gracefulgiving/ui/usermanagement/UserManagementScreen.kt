package com.gracechurch.gracefulgiving.ui.usermanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInviteDialog by remember { mutableStateOf(false) }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.7f)

    Scaffold(
        topBar = { TopAppBar(title = { Text("User Management", style = textStyle) }) },
        floatingActionButton = {
            if (uiState.currentUserRole == UserRole.ADMIN) {
                FloatingActionButton(onClick = { showInviteDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Invite User")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text(
                    text = "Tap a user to edit, or press the + button to invite a new user.",
                    modifier = Modifier.padding(16.dp),
                    style = textStyle
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.users) { user ->
                        UserItem(
                            user = user,
                            onDelete = { viewModel.deleteUser(user) },
                            onEdit = { navController.navigate("edit_user/${user.id}") },
                            canEdit = uiState.currentUserRole == UserRole.ADMIN,
                            textStyle = textStyle
                        )
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteUserDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = {
                viewModel.inviteUser(it)
                showInviteDialog = false
            },
            textStyle = textStyle
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onDelete: (User) -> Unit,
    onEdit: () -> Unit,
    canEdit: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = canEdit, onClick = onEdit)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(user.avatarUri ?: Icons.Default.Person),
                contentDescription = "Avatar",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = user.fullName, style = textStyle)
                Text(text = user.email, style = textStyle)
                Text(text = "Role: ${user.role}", style = textStyle)
            }
            if (canEdit) {
                IconButton(onClick = { onDelete(user) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete User")
                }
            }
        }
    }
}

@Composable
fun InviteUserDialog(
    onDismiss: () -> Unit,
    onInvite: (InviteUser) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.USER) }
    var avatarUri by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite User", style = textStyle) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberAsyncImagePainter(avatarUri ?: Icons.Default.Person),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row {
                            IconButton(onClick = { avatarUri = "male" }) {
                                Icon(Icons.Default.Person, contentDescription = "Male Avatar")
                            }
                            IconButton(onClick = { avatarUri = "female" }) {
                                Icon(Icons.Default.Face, contentDescription = "Female Avatar")
                            }
                        }
                        Row {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Take Picture")
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "From Clipboard")
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    textStyle = textStyle
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    textStyle = textStyle
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    textStyle = textStyle
                )
                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Temporary Password") },
                    textStyle = textStyle
                )
                Row {
                    Text("Role:", style = textStyle)
                    RadioButton(
                        selected = role == UserRole.USER,
                        onClick = { role = UserRole.USER }
                    )
                    Text("User", style = textStyle)
                    RadioButton(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN }
                    )
                    Text("Admin", style = textStyle)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onInvite(InviteUser(email, username, fullName, role, tempPassword, avatarUri))
                }
            ) {
                Text("Invite", style = textStyle)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel", style = textStyle)
            }
        }
    )
}
