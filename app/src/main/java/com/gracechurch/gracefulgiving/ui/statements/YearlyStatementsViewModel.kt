package com.gracechurch.gracefulgiving.ui.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

    private var allDonations: List<Donation> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch all donors and donations just once
                val donors = donorRepository.getAllDonors()
                // Use .first() to get a single snapshot of the donations for initial setup
                allDonations = donationRepository.getAllDonations().first()

                // Extract available years from all donations
                val years = allDonations.map { donation ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = donation.checkDate // Use checkDate from Donation model
                    calendar.get(Calendar.YEAR).toString()
                }.distinct().sortedDescending() // Sort years with newest first

                _uiState.update { it.copy(donors = donors, years = years, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onYearSelected(year: String) {
        _uiState.update { it.copy(selectedYear = year) }
        onDonorSelected(_uiState.value.selectedDonorId)
    }

    fun onDonorSelected(donorId: Long?) {
        _uiState.update { it.copy(selectedDonorId = donorId) }

        if (donorId == null || donorId <= 0L) {
            // If donor is deselected, clear the donations
            _uiState.update { it.copy(selectedDonorDonations = emptyList()) }
            return
        }

        // Filter the locally stored donations based on donorId and selectedYear
        val filteredDonations = allDonations.filter { donation ->
            val calendar = Calendar.getInstance().apply { timeInMillis = donation.checkDate }
            val donationYear = calendar.get(Calendar.YEAR).toString()
            donation.donorId == donorId && donationYear == _uiState.value.selectedYear
        }

        _uiState.update { it.copy(selectedDonorDonations = filteredDonations) }
    }
}

data class YearlyStatementsUiState(
    val donors: List<Donor> = emptyList(),
    val selectedDonorDonations: List<Donation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val years: List<String> = emptyList(),
    val selectedYear: String? = null,
    val selectedDonorId: Long? = null // Keep track of the selected donor
)
