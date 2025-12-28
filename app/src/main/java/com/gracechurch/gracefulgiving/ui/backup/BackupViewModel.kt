package com.gracechurch.gracefulgiving.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.usecase.BackupDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BackupViewModel @Inject constructor(
    private val backupDatabaseUseCase: BackupDatabaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun backupDatabase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
            try {
                val filePath = backupDatabaseUseCase.execute()
                _uiState.value = BackupUiState(isSuccess = true, filePath = filePath)
            } catch (e: Exception) {
                _uiState.value = BackupUiState(error = e.message ?: "Backup failed")
            }
        }
    }

    fun clearState() {
        _uiState.value = BackupUiState()
    }
}
