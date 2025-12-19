package com.gracechurch.gracefulgiving.ui.import

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class ImportDonationsViewModel(
    private val importDonationsUseCase: ImportDonationsUseCase,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onFileSelected(inputStream: InputStream?) {
        if (inputStream == null) {
            _uiState.value = _uiState.value.copy(error = "Could not open file")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
            try {
                val userId = userSessionRepository.currentUser?.id ?: 0L
                importDonationsUseCase.execute(inputStream, userId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun clearState() {
        _uiState.value = ImportUiState()
    }
}