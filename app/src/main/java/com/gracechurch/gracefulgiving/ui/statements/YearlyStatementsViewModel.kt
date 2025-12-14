package com.gracechurch.gracefulgiving.ui.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
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
    private val donationRepository: DonationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YearlyStatementsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val donors = donorRepository.getAllDonors()

                // Collect the Flow to get the list
                val allDonations = donationRepository.getAllDonations().first()

                val years = allDonations.map { donation ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = donation.checkDate  // Changed from donation.date
                    calendar.get(Calendar.YEAR).toString()
                }.distinct().sortedDescending()

                // Set the most recent year as default
                val defaultYear = years.firstOrNull()

                _uiState.update {
                    it.copy(
                        donors = donors,
                        years = years,
                        selectedYear = defaultYear,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onYearSelected(year: String) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun onDonorSelected(donorId: Long) {
        if (donorId == 0L) {
            // Clear selection
            _uiState.update { it.copy(selectedDonorDonations = emptyList()) }
            return
        }

        viewModelScope.launch {
            try {
                // Collect the Flow and filter by selected year
                donationRepository.getDonationsByDonor(donorId).collect { donations ->
                    val filteredDonations = if (_uiState.value.selectedYear != null) {
                        donations.filter { donation ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = donation.checkDate  // Changed from donation.date
                            calendar.get(Calendar.YEAR).toString() == _uiState.value.selectedYear
                        }
                    } else {
                        donations
                    }
                    _uiState.update { it.copy(selectedDonorDonations = filteredDonations) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun generateStatement(donorId: Long) {
        // TODO: Implement PDF generation for the selected donor's yearly statement
    }
}

data class YearlyStatementsUiState(
    val donors: List<com.gracechurch.gracefulgiving.domain.model.Donor> = emptyList(),
    val selectedDonorDonations: List<Donation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val years: List<String> = emptyList(),
    val selectedYear: String? = null
)