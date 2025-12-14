package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.domain.repository.BankSettingsRepository
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchEntryViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val bankSettingsRepository: BankSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEntryUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            batchRepository.getBatch(batchId).collect { batch ->
                _uiState.update { it.copy(batchWithDonations = batch) }
            }
        }
        viewModelScope.launch {
            bankSettingsRepository.getBankSettings().collect { settings ->
                _uiState.update { it.copy(bankSettings = settings) }
            }
        }
    }

    fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        viewModelScope.launch {
            batchRepository.addDonation(firstName, lastName, checkNumber, amount, date, image, batchId)
        }
    }

    fun deleteDonation(donationId: Long) {
        viewModelScope.launch {
            batchRepository.deleteDonation(donationId)
        }
    }

    fun updateDonation(donation: DonationEntity) {
        viewModelScope.launch {
            batchRepository.updateDonation(donation)
        }
    }

    fun setScannedData(data: ScannedCheckData) {
        _uiState.update { it.copy(scannedData = data) }
    }

    fun clearScannedData() {
        _uiState.update { it.copy(scannedData = null) }
    }

    fun closeBatch(batchId: Long) {
        viewModelScope.launch {
            batchRepository.closeBatch(batchId)
        }
    }
}

data class BatchEntryUiState(
    val batchWithDonations: BatchWithDonations? = null,
    val scannedData: ScannedCheckData? = null,
    val bankSettings: BankSettingsEntity? = null
)
