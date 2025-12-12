package com.gracechurch.gracefulgiving.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.data.repository.BankSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BankSettingsViewModel @Inject constructor(
    private val repository: BankSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBankSettings()
    }

    private fun loadBankSettings() {
        viewModelScope.launch {
            repository.getBankSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun saveBankSettings(
        bankName: String,
        accountName: String,
        accountNumber: String,
        routingNumber: String
    ) {
        viewModelScope.launch {
            try {
                repository.saveBankSettings(
                    bankName = bankName.trim(),
                    accountName = accountName.trim(),
                    accountNumber = accountNumber.trim(),
                    routingNumber = routingNumber.trim()
                )
                _uiState.update {
                    it.copy(
                        successMessage = "Bank settings saved successfully",
                        error = null
                    )
                }

                // Clear success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to save settings: ${e.message}",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class BankSettingsUiState(
    val settings: BankSettingsEntity? = null,
    val error: String? = null,
    val successMessage: String? = null
)