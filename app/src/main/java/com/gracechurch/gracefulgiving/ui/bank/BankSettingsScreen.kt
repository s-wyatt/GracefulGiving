package com.gracechurch.gracefulgiving.ui.bank

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSettingsScreen(
    vm: BankSettingsViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Settings") },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = vm::onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Settings")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.settings.bankName,
                onValueChange = vm::onBankNameChanged,
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !uiState.isEditing
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.settings.accountName,
                onValueChange = vm::onAccountNameChanged,
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !uiState.isEditing
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.settings.accountNumber,
                onValueChange = vm::onAccountNumberChanged,
                label = { Text("Account Number") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !uiState.isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (uiState.isEditing) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = vm::onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = vm::onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
