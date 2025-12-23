package com.gracechurch.gracefulgiving.ui.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.DonationListItem
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository

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
        val donations = donationRepository.getAllDonations().first()
        _uiState.value = DonorsDonationsUiState(donors, donations)
    }
}

data class DonorsDonationsUiState(
    val donors: List<Donor> = emptyList(),
    val donations: List<DonationListItem> = emptyList()
)