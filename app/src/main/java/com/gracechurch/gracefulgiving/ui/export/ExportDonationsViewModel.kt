package com.gracechurch.gracefulgiving.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.usecase.ExportDonationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExportDonationsViewModel @Inject constructor(
    private val exportDonationsUseCase: ExportDonationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun exportDonations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
            try {
                val filePath = exportDonationsUseCase.execute()
                _uiState.value = ExportUiState(isSuccess = true, filePath = filePath)
            } catch (e: Exception) {
                _uiState.value = ExportUiState(error = e.message ?: "Export failed")
            }
        }
    }

    fun clearState() {
        _uiState.value = ExportUiState()
    }
}