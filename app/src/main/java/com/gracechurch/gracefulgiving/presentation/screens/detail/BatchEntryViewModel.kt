package com.gracechurch.gracefulgiving.presentation.screens.detail
import BatchWithDonations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.repository.*
import com.gracechurch.gracefulgiving.domain.model.*
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

    fun createBatch(batchDate: Long, userId: Long) {
        viewModelScope.launch {
            val batch: BatchWithDonations? = batchRepo.createBatch(batchDate, userId)
            _uiState.update { it.copy(currentBatch = batch) }
        }
    }

    fun addDonation(firstName: String, lastName: String, checkNumber: String, amount: Double, checkDate: Long, imageData: String? = null) {
        viewModelScope.launch {
            val donor = donorRepo.findOrCreateDonor(firstName, lastName)
            val batch = _uiState.value.currentBatch ?: return@launch
            val donation = Donation(donorId = donor.id, donorName = donor.fullName, batchId = batch.id, checkNumber = checkNumber, checkDate = checkDate, amount = amount)
            batchRepo.addDonation(donation, imageData)
            loadBatch(batch.id)
        }
    }

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            batchRepo.getBatchWithDonations(batchId).collect { batch ->
                _uiState.update { it.copy(currentBatch = batch) }
            }
        }
    }

    fun setScannedData(data: ScannedCheckData) { _uiState.update { it.copy(scannedData = data) } }
    fun clearScannedData() { _uiState.update { it.copy(scannedData = null) } }
}

data class BatchEntryUiState(val currentBatch: BatchWithDonations? = null, val scannedData: ScannedCheckData? = null)