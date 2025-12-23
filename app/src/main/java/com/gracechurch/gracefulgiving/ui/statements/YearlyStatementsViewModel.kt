package com.gracechurch.gracefulgiving.ui.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.DonationListItem
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class YearlyStatementsViewModel @Inject constructor(
    private val donorRepository: DonorRepository,
    private val donationRepository: DonationRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YearlyStatementsUiState())
    val uiState = _uiState.asStateFlow()

    private var allDonations: List<DonationListItem> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val donors = donorRepository.getAllDonors()
                allDonations = donationRepository.getAllDonations().first()

                val years = allDonations.map { donation ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = donation.checkDate
                    calendar.get(Calendar.YEAR).toString()
                }.distinct().sortedDescending()

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                val defaultYear = if (currentMonth > Calendar.FEBRUARY || (currentMonth == Calendar.FEBRUARY && currentDay > 10)) {
                    currentYear.toString()
                } else {
                    (currentYear - 1).toString()
                }

                val yearToSelect = when {
                    years.isEmpty() -> null
                    years.contains(defaultYear) -> defaultYear
                    else -> years.firstOrNull()
                }

                val isAdmin = userSessionRepository.currentUser?.role == UserRole.ADMIN

                _uiState.update { it.copy(donors = donors, years = years, isLoading = false, isAdmin = isAdmin) }

                if (yearToSelect != null) {
                    onYearSelected(yearToSelect)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onYearSelected(year: String) {
        val donationsForYear = allDonations.filter { donation ->
            val calendar = Calendar.getInstance().apply { timeInMillis = donation.checkDate }
            calendar.get(Calendar.YEAR).toString() == year
        }

        val donorIdsWithDonations = donationsForYear
            .groupBy { it.donorId }
            .filter { (_, donations) -> donations.sumOf { it.checkAmount } > 0 }
            .keys

        _uiState.update { it.copy(
            selectedYear = year,
            donationsForSelectedYear = donationsForYear,
            selectedDonorIdsForPrint = donorIdsWithDonations
        ) }
    }

    fun onDonorSelected(donorId: Long?) {
        _uiState.update { it.copy(selectedDonorId = donorId) }
        if (donorId == null || donorId <= 0L) {
            _uiState.update { it.copy(selectedDonorDonations = emptyList()) }
            return
        }

        val selectedYear = _uiState.value.selectedYear
        val filteredDonations = allDonations.filter { donation ->
            val calendar = Calendar.getInstance().apply { timeInMillis = donation.checkDate }
            val donationYear = calendar.get(Calendar.YEAR).toString()
            donation.donorId == donorId && donationYear == selectedYear
        }
        _uiState.update { it.copy(selectedDonorDonations = filteredDonations) }
    }

    fun toggleDonorSelection(donorId: Long) {
        _uiState.update { currentState ->
            val currentSelection = currentState.selectedDonorIdsForPrint.toMutableSet()
            if (currentSelection.contains(donorId)) currentSelection.remove(donorId) else currentSelection.add(donorId)
            currentState.copy(selectedDonorIdsForPrint = currentSelection)
        }
    }

    fun selectAllDonors() {
        val relevantDonors = _uiState.value.donationsForSelectedYear
            .groupBy { it.donorId }
            .keys
        _uiState.update { it.copy(selectedDonorIdsForPrint = relevantDonors) }
    }

    fun clearAllDonors() {
        _uiState.update { it.copy(selectedDonorIdsForPrint = emptySet()) }
    }

    fun setSortType(sortType: SortType) {
        _uiState.update { it.copy(sortType = sortType) }
    }

    fun onMergeClicked() {
        _uiState.update { it.copy(showMergeDialog = true) }
    }

    fun onDismissMergeDialog() {
        _uiState.update { it.copy(showMergeDialog = false) }
    }

    fun mergeDonors(destinationDonorId: Long) {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedDonorIdsForPrint
            if (selectedIds.size != 2) return@launch

            val sourceDonorId = selectedIds.first { it != destinationDonorId }

            try {
                donationRepository.moveDonations(sourceDonorId, destinationDonorId)
                // donorRepository.deleteDonorById(sourceDonorId)
                // Refresh data
                onDismissMergeDialog()
                loadInitialData()
            } catch(e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

enum class SortType {
    NAME_ASCENDING,
    TOTAL_DESCENDING,
    TOTAL_ASCENDING
}

data class YearlyStatementsUiState(
    val donors: List<Donor> = emptyList(),
    val selectedDonorDonations: List<DonationListItem> = emptyList(),
    val donationsForSelectedYear: List<DonationListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val years: List<String> = emptyList(),
    val selectedYear: String? = null,
    val selectedDonorId: Long? = null,
    val selectedDonorIdsForPrint: Set<Long> = emptySet(),
    val isAdmin: Boolean = false,
    val sortType: SortType = SortType.NAME_ASCENDING,
    val showMergeDialog: Boolean = false
)
