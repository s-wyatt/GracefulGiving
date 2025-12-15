package com.gracechurch.gracefulgiving.ui.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DonorDetailViewModel @Inject constructor(
    private val donorRepository: DonorRepository,
    private val donationRepository: DonationRepository
) : ViewModel() {

    private val _donor = MutableStateFlow<Donor?>(null)
    val donor: StateFlow<Donor?> = _donor

    private val _donations = MutableStateFlow<List<Donation>>(emptyList())
    val donations: StateFlow<List<Donation>> = _donations

    fun loadDonor(donorId: Long) {
        viewModelScope.launch {
            _donor.value = donorRepository.getDonorById(donorId)
        }
    }

    fun loadDonations(donorId: Long) {
        viewModelScope.launch {
            _donations.value = donationRepository.getDonationsByDonorId(donorId)
        }
    }

    fun updateDonor(donor: Donor) {
        viewModelScope.launch {
            donorRepository.updateDonor(donor)
        }
    }
}