package com.gracechurch.gracefulgiving.ui.donors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gracechurch.gracefulgiving.data.repository.DonorRepository
import com.gracechurch.gracefulgiving.data.repository.DonationRepository

@HiltViewModel
class DonorsDonationsViewModel @Inject constructor(
    private val donorRepository: DonorRepository,
    private val donationRepository: DonationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonorsDonationsUiState())
    val uiState: StateFlow<DonorsDonationsUiState> = _uiState

    init { load() }

    private fun load() = viewModelScope.launch {
        val donors = donorRepository.getAllDonors()
        val donations = donationRepository.getAllDonations()
        _uiState.value = DonorsDonationsUiState(donors, donations)
    }
}

data class DonorsDonationsUiState(
    val donors: List<Donor> = emptyList(),
    val donations: List<Donation> = emptyList()
)

data class Donor(val id: Long, val name: String)

data class Donation(val id: Long, val donorId: Long, val amount: Double)
