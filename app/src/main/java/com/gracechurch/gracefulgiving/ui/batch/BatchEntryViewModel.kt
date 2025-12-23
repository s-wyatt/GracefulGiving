package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Batch
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BatchEntryViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val fundRepository: FundRepository,
    private val donorRepository: DonorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEntryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAllDonors()
        loadAllFunds()
    }

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

    private fun loadAllDonors() {
        viewModelScope.launch {
            val donors = withContext(Dispatchers.IO) {
                donorRepository.getAllDonors()
            }
            _uiState.update { it.copy(donors = donors) }
        }
    }

    private fun loadAllFunds() {
        viewModelScope.launch {
            val funds = withContext(Dispatchers.IO) {
                fundRepository.getAllFundsOneShot()
            }
            _uiState.update { it.copy(funds = funds) }
        }
    }

    fun updateBatchDetails(batchId: Long, date: Long, fundId: Long) {
        viewModelScope.launch {
            val currentBatchEntity = _uiState.value.batchWithDonations?.batch
            if (currentBatchEntity != null) {
                val updatedBatch = Batch(
                    batchId = currentBatchEntity.batchId,
                    batchNumber = currentBatchEntity.batchNumber.toString(),
                    batchDate = date,
                    createdBy = currentBatchEntity.userId,
                    createdOn = currentBatchEntity.createdOn, 
                    status = currentBatchEntity.status,
                    fundId = fundId
                )
                batchRepository.updateBatch(updatedBatch)
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
        batchId: Long,
        overrideDonorId: Long? = null
    ) {
        viewModelScope.launch {
            val fundId = _uiState.value.batchWithDonations?.batch?.fundId
            if (fundId != null) {
                batchRepository.addDonation(
                    firstName = firstName, 
                    lastName = lastName, 
                    checkNumber = checkNumber, 
                    amount = amount, 
                    date = date, 
                    image = image, 
                    batchId = batchId, 
                    fundId = fundId,
                    donorId = overrideDonorId
                )
            }
        }
    }

    fun addAlias(donorId: Long, firstName: String, lastName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                donorRepository.addAlias(donorId, firstName, lastName)
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
        viewModelScope.launch {
            val exactMatch = if (data.firstName != null && data.lastName != null) {
                withContext(Dispatchers.IO) {
                    donorRepository.findDonorByAlias(data.firstName!!, data.lastName!!)
                }
            } else {
                null
            }
            
            val finalMatch = if (exactMatch != null) {
                exactMatch
            } else {
                val donors = _uiState.value.donors
                donors.find { it.lastName.equals(data.lastName, ignoreCase = true) }
            }
            
            _uiState.update { it.copy(scannedData = data, matchedDonor = finalMatch) }
        }
    }

    fun clearScannedData() {
        _uiState.update { it.copy(scannedData = null, matchedDonor = null) }
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
    val fund: Fund? = null,
    val donors: List<Donor> = emptyList(),
    val funds: List<Fund> = emptyList(),
    val matchedDonor: Donor? = null
)
