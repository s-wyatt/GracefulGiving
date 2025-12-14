package com.gracechurch.gracefulgiving.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.domain.repository.BankSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BankSettingsViewModel @Inject constructor(
    private val repository: BankSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getBankSettings().collect { settingsFromDb ->
                val settings = settingsFromDb ?: BankSettingsEntity()
                _uiState.update {
                    it.copy(
                        settings = settings,
                        originalSettings = settings,
                        isEditing = false
                    )
                }
            }
        }
    }

    fun onEdit() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun onCancel() {
        _uiState.update {
            it.copy(
                settings = it.originalSettings ?: BankSettingsEntity(),
                isEditing = false
            )
        }
    }

    fun onSave() {
        viewModelScope.launch {
            repository.saveBankSettings(_uiState.value.settings)
            // Flow collection in loadSettings will update state and turn off edit mode
        }
    }

    fun onBankNameChanged(name: String) {
        _uiState.update { it.copy(settings = it.settings.copy(bankName = name)) }
    }

    fun onAccountNameChanged(name: String) {
        _uiState.update { it.copy(settings = it.settings.copy(accountName = name)) }
    }

    fun onAccountNumberChanged(number: String) {
        _uiState.update { it.copy(settings = it.settings.copy(accountNumber = number)) }
    }
}

data class BankSettingsUiState(
    val settings: BankSettingsEntity = BankSettingsEntity(),
    val originalSettings: BankSettingsEntity? = null,
    val isEditing: Boolean = false
)
