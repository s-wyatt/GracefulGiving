package com.gracechurch.gracefulgiving.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.data.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchEntryViewModel @Inject constructor(
    private val batchRepo: BatchRepository
    // donorRepo is not used, so it can be removed
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEntryUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Loads a batch with all its donations.
     * This is called by the UI when it's created with a specific batchId.
     */
    fun loadBatch(batchId: Long) {
        // Do nothing if the batch is already loaded
        if (_uiState.value.batchWithDonations?.batch?.batchId == batchId) return

        viewModelScope.launch {
            try {
                batchRepo.getBatch(batchId).collect { batchWithDonations ->
                    _uiState.update {
                        it.copy(
                            batchWithDonations = batchWithDonations,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Adds a new donation to the specified batch.
     * The signature is updated to accept the batchId directly from the UI.
     */
    fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        checkDate: Long,
        imageData: String?,
        batchId: Long // <-- CORRECTED: Added batchId parameter
    ) {
        // Ensure we have a valid batchId before proceeding
        if (batchId <= 0L) {
            _uiState.update { it.copy(error = "Cannot add donation to an invalid batch.") }
            return
        }

        viewModelScope.launch {
            try {
                // Add the donation using the provided batchId
                batchRepo.addDonation(
                    firstName = firstName,
                    lastName = lastName,
                    checkNumber = checkNumber,
                    amount = amount,
                    date = checkDate,
                    image = imageData,
                    batchId = batchId // Pass the correct batchId
                )

                // No need to reload the batch here, as the Flow from getBatch()
                // will automatically emit the new state.
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Sets scanned check data from the camera.
     */
    fun setScannedData(data: ScannedCheckData) {
        _uiState.update { it.copy(scannedData = data) }
    }

    /**
     * Clears scanned check data.
     */
    fun clearScannedData() {
        _uiState.update { it.copy(scannedData = null) }
    }

    /**
     * Clears any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // The createBatch function is no longer needed here, as batch creation is now
    // handled by BatchSelectionViewModel. It can be safely removed.
}

/**
 * UI State for the Batch Entry screen
 */
data class BatchEntryUiState(
    val batchWithDonations: BatchWithDonations? = null,
    val scannedData: ScannedCheckData? = null,
    val error: String? = null
)
