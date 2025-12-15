package com.gracechurch.gracefulgiving.ui.donation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val donationRepository: DonationRepository
) : ViewModel() {

    private val _donation = MutableStateFlow<Donation?>(null)
    val donation: StateFlow<Donation?> = _donation

    fun loadDonation(donationId: Long) {
        viewModelScope.launch {
            _donation.value = donationRepository.getDonationById(donationId)
        }
    }

    fun updateDonation(donation: Donation) {
        viewModelScope.launch {
            donationRepository.updateDonation(donation)
        }
    }

    fun getCheckImage(path: String): Flow<Bitmap?> = flow {
        val file = File(path)
        if (file.exists()) {
            emit(BitmapFactory.decodeFile(file.absolutePath))
        } else {
            emit(null)
        }
    }
}