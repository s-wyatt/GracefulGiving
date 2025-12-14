package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchSelectionViewModel @Inject constructor(
    private val batchRepo: BatchRepository
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

    // GENTLE FIX: Update the function to accept the selected date.
    fun createNewBatch(userId: Long, createdOn: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Now pass both parameters to the repository function.
                val batchId = batchRepo.createBatch(userId, createdOn)
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

    fun printDepositSlip(batchId: Long) {
        // TODO: Implement deposit slip printing
        viewModelScope.launch {
            _uiState.update { it.copy(error = "Print deposit slip not yet implemented") }
        }
    }
}

data class BatchSelectionUiState(
    val batches: List<BatchWithDonations> = emptyList(),
    val error: String? = null
)
