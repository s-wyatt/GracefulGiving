package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchEntryViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEntryUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            batchRepository.getBatch(batchId).collect { batchWithDonations ->
                _uiState.update { it.copy(batchWithDonations = batchWithDonations) }
                batchWithDonations?.batch?.fundId?.let { fundId ->
                    launch {
                        val fund = fundRepository.getFund(fundId)
                        _uiState.update { it.copy(fund = fund) }
                    }
                }
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
            val fundId = _uiState.value.batchWithDonations?.batch?.fundId
            if (fundId != null) {
                batchRepository.addDonation(firstName, lastName, checkNumber, amount, date, image, batchId, fundId)
            }
        }
    }

    fun deleteDonation(donationId: Long) {
        viewModelScope.launch {
            batchRepository.deleteDonation(donationId)
        }
    }

    fun updateDonation(donation: Donation) {
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
    val fund: Fund? = null
)
