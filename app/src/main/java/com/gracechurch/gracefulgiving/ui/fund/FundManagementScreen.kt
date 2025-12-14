package com.gracechurch.gracefulgiving.ui.fund

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracechurch.gracefulgiving.domain.model.Fund
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundManagementScreen(
    vm: FundManagementViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    var showFundDialog by remember { mutableStateOf(false) }
    var selectedFund by remember { mutableStateOf<Fund?>(null) }

    LaunchedEffect(Unit) {
        vm.loadFunds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fund Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showFundDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Fund")
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.funds) { fund ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fund.name, style = MaterialTheme.typography.titleMedium)
                            Text("Bank: ${'$'}{fund.bankName}", style = MaterialTheme.typography.bodySmall)
                            Text("Account: ${'$'}{fund.accountNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            selectedFund = fund
                            showFundDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Fund")
                        }
                    }
                }
            }
        }
    }

    if (showFundDialog) {
        FundDialog(
            fund = selectedFund,
            onDismiss = {
                showFundDialog = false
                selectedFund = null
            },
            onSave = {
                if (selectedFund == null) {
                    vm.addFund(it)
                } else {
                    vm.updateFund(it)
                }
                showFundDialog = false
                selectedFund = null
            }
        )
    }
}

@Composable
fun FundDialog(
    fund: Fund?,
    onDismiss: () -> Unit,
    onSave: (Fund) -> Unit
) {
    var name by remember { mutableStateOf(fund?.name ?: "") }
    var bankName by remember { mutableStateOf(fund?.bankName ?: "") }
    var accountName by remember { mutableStateOf(fund?.accountName ?: "") }
    var accountNumber by remember { mutableStateOf(fund?.accountNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (fund == null) "Add Fund" else "Edit Fund") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Fund Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newFund = fund?.copy(
                        name = name,
                        bankName = bankName,
                        accountName = accountName,
                        accountNumber = accountNumber
                    ) ?: Fund(
                        name = name,
                        bankName = bankName,
                        accountName = accountName,
                        accountNumber = accountNumber
                    )
                    onSave(newFund)
                },
                enabled = name.isNotBlank() && bankName.isNotBlank() && accountName.isNotBlank() && accountNumber.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}