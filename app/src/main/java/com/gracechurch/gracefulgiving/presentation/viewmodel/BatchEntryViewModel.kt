package com.gracechurch.gracefulgiving.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.data.repository.BatchRepository
import com.gracechurch.gracefulgiving.data.repository.DonorRepository
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchEntryViewModel @Inject constructor(
    private val batchRepo: BatchRepository,
    private val donorRepo: DonorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEntryUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Creates a new batch and loads it with donations
     */
    fun createBatch(batchDate: Long, userId: Long) {
        viewModelScope.launch {
            try {
                // createBatch returns the ID of the newly created batch
                val batchId = batchRepo.createBatch(batchDate, userId)

                // Load the batch with its donations
                loadBatch(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Adds a new donation to the current batch
     */
    fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        checkDate: Long,
        imageData: String? = null
    ) {
        viewModelScope.launch {
            try {
                val batchWithDonations = _uiState.value.batchWithDonations
                val batchId = batchWithDonations?.batch?.batchId ?: run {
                    _uiState.update { it.copy(error = "No active batch") }
                    return@launch
                }

                // Add the donation (repository handles donor creation/lookup)
                batchRepo.addDonation(
                    firstName = firstName,
                    lastName = lastName,
                    checkNumber = checkNumber,
                    amount = amount,
                    date = checkDate,
                    image = imageData,
                    batchId = batchId
                )

                // Reload the batch to show the new donation
                loadBatch(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Loads a batch with all its donations
     */
    private fun loadBatch(batchId: Long) {
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
     * Sets scanned check data from the camera
     */
    fun setScannedData(data: ScannedCheckData) {
        _uiState.update { it.copy(scannedData = data) }
    }

    /**
     * Clears scanned check data
     */
    fun clearScannedData() {
        _uiState.update { it.copy(scannedData = null) }
    }

    /**
     * Clears any error messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State for the Batch Entry screen
 */
data class BatchEntryUiState(
    val batchWithDonations: BatchWithDonations? = null,
    val scannedData: ScannedCheckData? = null,
    val error: String? = null
)