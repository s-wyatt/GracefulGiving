package com.gracechurch.gracefulgiving.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * App header that displays current user info and updates live
 * when the user edits their profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    viewModel: AppHeaderViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()

    TopAppBar(
        title = {
            Text("Graceful Giving")
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onProfileClick)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentUser!!.fullName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    UserAvatar(
                        avatarUri = currentUser!!.avatarUri,
                        fullName = currentUser!!.fullName,
                        size = 40.dp
                    )
                }
            }
        }
    )
}