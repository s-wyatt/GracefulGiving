package com.gracechurch.gracefulgiving.ui.bank

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes
import com.gracechurch.gracefulgiving.presentation.viewmodel.BankSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSettingsScreen(
    navController: NavController,
    vm: BankSettingsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    var bankName by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var routingNumber by remember { mutableStateOf("") }
    var showAccountNumber by remember { mutableStateOf(false) }
    var showRoutingNumber by remember { mutableStateOf(false) }

    LaunchedEffect(state.settings) {
        state.settings?.let { settings ->
            bankName = settings.bankName
            accountName = settings.accountName
            accountNumber = settings.accountNumber
            routingNumber = settings.routingNumber
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Bank Account Information", style = MaterialTheme.typography.titleLarge)

            Text(
                "This information will be used for deposit slips and reports.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                placeholder = { Text("e.g., First National Bank") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., Grace Church Operating Account") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Account Number") },
                placeholder = { Text("Enter account number") },
                visualTransformation = if (showAccountNumber) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showAccountNumber = !showAccountNumber }) {
                        Text(if (showAccountNumber) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = routingNumber,
                onValueChange = { routingNumber = it },
                label = { Text("Routing Number") },
                placeholder = { Text("9-digit routing number") },
                visualTransformation = if (showRoutingNumber) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showRoutingNumber = !showRoutingNumber }) {
                        Text(if (showRoutingNumber) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.saveBankSettings(
                        bankName = bankName,
                        accountName = accountName,
                        accountNumber = accountNumber,
                        routingNumber = routingNumber
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = { navController.navigate(Routes.DASHBOARD) }) {
                Text("Back to Dashboard")
            }

            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            state.successMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}