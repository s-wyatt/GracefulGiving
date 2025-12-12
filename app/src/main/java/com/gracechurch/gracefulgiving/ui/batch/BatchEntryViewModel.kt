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
class BatchManagementViewModel @Inject constructor(
    private val batchRepo: BatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchManagementUiState())
    val uiState = _uiState.asStateFlow()

    // This is the function where your code snippet belongs
    fun loadBatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // The logic to collect the flow from the repository
            batchRepo.getAllBatches().collect { batches ->
                _uiState.update {
                    it.copy(
                        batches = batches,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun createNewBatch(userId: Long, onBatchCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newBatchId = batchRepo.createBatch(userId)
            if (newBatchId > 0) {
                onBatchCreated(newBatchId)
            } else {
                _uiState.update { it.copy(error = "Failed to create new batch.") }
            }
        }
    }

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            batchRepo.deleteBatch(batchId)
            // The flow will automatically update the list
        }
    }

    // You would add functions for sorting and filtering here
    fun setSortType(sortType: String) { /* TODO */ }
    fun setFilterType(filterType: String) { /* TODO */ }
    fun printBatchReport(batchId: Long) { /* TODO */ }
    fun printDepositSlip(batchId: Long) { /* TODO */ }
}

// UI State for the Batch Management screen
data class BatchManagementUiState(
    val batches: List<BatchWithDonations> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortType: String = "Date", // Example property
    val filterType: String = "All", // Example property
    val filteredAndSorted: List<BatchWithDonations> = emptyList() // Example property
)
