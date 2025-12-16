package com.gracechurch.gracefulgiving.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchManagementViewModel @Inject constructor(
    private val repository: BatchRepository,
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchManagementUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBatches() {
        viewModelScope.launch {
            repository.getAllBatches().collect { batches ->
                _uiState.update { it.copy(batches = batches, isLoading = false) }
            }
        }
    }

    fun createNewBatch(createdOn: Long, onBatchCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val userId = userSessionRepository.currentUser?.id ?: return@launch
            val newBatchId = repository.createBatch(userId, createdOn)
            onBatchCreated(newBatchId)
        }
    }

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            val userId = userSessionRepository.currentUser?.id ?: return@launch
            val user = userRepository.getUserById(userId)
            val batch = repository.getBatch(batchId).first()
            if (batch?.batch?.status == "closed" && user?.role?.name != "ADMIN") {
                // Show error message
                return@launch
            }
            repository.deleteBatch(batchId)
        }
    }

    fun setSortType(sortType: SortType) {
        _uiState.update { it.copy(sortType = sortType) }
    }

    fun setFilterType(filterType: FilterType) {
        _uiState.update { it.copy(filterType = filterType) }
    }

    fun printBatchReport(batchId: Long) {
        // TODO: Implement printing logic
    }

    fun printDepositSlip(batchId: Long) {
        // TODO: Implement printing logic
    }
}

data class BatchManagementUiState(
    val batches: List<BatchWithDonations> = emptyList(),
    val isLoading: Boolean = true,
    val sortType: SortType = SortType.DateDescending,
    val filterType: FilterType = FilterType.All
) {
    val filteredAndSorted: List<BatchWithDonations>
        get() = batches
            .filter { filterType.matches(it) }
            .sortedWith(sortType.comparator)
}


enum class SortType(val comparator: Comparator<BatchWithDonations>) {
    DateDescending(compareByDescending { it.batch.createdOn }),
    DateAscending(compareBy { it.batch.createdOn }),
    AmountDescending(compareByDescending { it.donations.sumOf { d -> d.donation.checkAmount } }),
    AmountAscending(compareBy { it.donations.sumOf { d -> d.donation.checkAmount } })
}

enum class FilterType {
    All,
    Open,
    Closed;

    fun matches(batch: BatchWithDonations): Boolean {
        // TODO: Implement filter logic based on batch status
        return true
    }
}
