package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchSelectionViewModel @Inject constructor(
    private val batchRepo: BatchRepository,
    private val fundRepo: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchSelectionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBatches() {
        viewModelScope.launch {
            try {
                batchRepo.getAllBatches().collect { batches ->
                    _uiState.update { it.copy(batches = batches) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadFunds() {
        viewModelScope.launch {
            try {
                fundRepo.getFunds().collect { funds ->
                    _uiState.update { it.copy(funds = funds) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun createNewBatch(userId: Long, createdOn: Long, fundId: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val batchId = batchRepo.createBatch(userId, createdOn, fundId)
                onCreated(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun printBatchReport(batchId: Long) {
        // TODO: Implement batch report printing
        viewModelScope.launch {
            _uiState.update { it.copy(error = "Print report not yet implemented") }
        }
    }
    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            try {
                batchRepo.deleteBatch(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    fun printDepositSlip(batchId: Long) {
        // TODO: Implement deposit slip printing
        viewModelScope.launch {
            _uiState.update { it.copy(error = "Print deposit slip not yet implemented") }
        }
    }
}

data class BatchSelectionUiState(
    val batches: List<BatchWithDonations> = emptyList(),
    val funds: List<Fund> = emptyList(),
    val error: String? = null
)
