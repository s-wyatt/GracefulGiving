package com.gracechurch.gracefulgiving.ui.batch

import androidx.compose.ui.geometry.isEmpty
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
    private val repo: BatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchManagementUiState())
    val uiState: StateFlow<BatchManagementUiState> = _uiState

    // Keep track of sort and filter types internally
    private val _sortType = MutableStateFlow(SortType.DATE_NEWEST)
    private val _filterType = MutableStateFlow(FilterType.ALL)

    init {
        // GENTLE FIX: Collect the flow of batches reactively.
        // This will automatically update whenever the data changes.
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine the batches flow with sort/filter flows
            combine(repo.getAllBatches(), _sortType, _filterType) { batches, sort, filter ->
                applyFiltersAndSorting(batches, sort, filter)
            }.collect { filteredAndSortedBatches ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        // Update the raw list and the filtered/sorted list
                        batches = filteredAndSortedBatches,
                        filteredAndSorted = filteredAndSortedBatches,
                        sortType = _sortType.value,
                        filterType = _filterType.value
                    )
                }
            }
        }
    }

    // loadBatches() is no longer needed, as the init block handles it.

    fun createNewBatch(userId: Long, navigate: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.createBatch(userId)
            navigate(id)
        }
    }

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            repo.deleteBatch(batchId)
            // No need to call loadBatches()! The Flow will automatically update the UI.
        }
    }

    fun printBatchReport(batchId: Long) {
        viewModelScope.launch { repo.generateBatchReport(batchId) }
    }

    fun printDepositSlip(batchId: Long) {
        viewModelScope.launch { repo.generateDepositSlip(batchId) }
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
        // The `combine` operator in the init block will automatically re-trigger.
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
        // The `combine` operator in the init block will also re-trigger.
    }

    private fun applyFiltersAndSorting(
        list: List<BatchWithDonations>,
        sort: SortType,
        filter: FilterType
    ): List<BatchWithDonations> {
        var out = list

        // Filtering
        out = when (filter) {
            FilterType.ALL -> out
            FilterType.EMPTY -> out.filter { it.donations.isEmpty() }
            FilterType.NON_EMPTY -> out.filter { it.donations.isNotEmpty() }
        }

        // Sorting
        out = when (sort) {
            SortType.DATE_NEWEST -> out.sortedByDescending { it.batch.createdOn }
            SortType.DATE_OLDEST -> out.sortedBy { it.batch.createdOn }
            SortType.TOTAL_ASC -> out.sortedBy { it.donations.sumOf { d -> d.donation.checkAmount } }
            SortType.TOTAL_DESC -> out.sortedByDescending { it.donations.sumOf { d -> d.donation.checkAmount } }
        }

        return out
    }
}

// ------------------------------ UI STATE MODEL ------------------------------
data class BatchManagementUiState(
    val isLoading: Boolean = false,
    val batches: List<BatchWithDonations> = emptyList(),
    val filteredAndSorted: List<BatchWithDonations> = emptyList(),
    val sortType: SortType = SortType.DATE_NEWEST,
    val filterType: FilterType = FilterType.ALL
)

// ------------------------------ SORT & FILTER ENUMS ------------------------------
enum class SortType(val label: String) {
    DATE_NEWEST("Newest First"),
    DATE_OLDEST("Oldest First"),
    TOTAL_ASC("Total Amount Asc"),
    TOTAL_DESC("Total Amount Desc")
}

enum class FilterType(val label: String) {
    ALL("All Batches"),
    EMPTY("Empty Only"),
    NON_EMPTY("With Donations")
}

