package com.gracechurch.gracefulgiving.ui.donation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val donationRepository: DonationRepository,
    private val donorRepository: DonorRepository
) : ViewModel() {

    private val _donation = MutableStateFlow<Donation?>(null)
    val donation: StateFlow<Donation?> = _donation

    private val _currentDonor = MutableStateFlow<Donor?>(null)
    val currentDonor: StateFlow<Donor?> = _currentDonor

    private val _donors = MutableStateFlow<List<Donor>>(emptyList())
    val donors: StateFlow<List<Donor>> = _donors

    fun loadDonation(donationId: Long) {
        viewModelScope.launch {
            Log.d("DonationViewModel", "Loading donation details for ID: $donationId")
            
            // 1. Fetch the lightweight donation item first (without image)
            val donationItem = withContext(Dispatchers.IO) {
                donationRepository.getDonationById(donationId)
            }

            if (donationItem != null) {
                // Update state immediately so UI shows data
                _donation.value = donationItem
                
                // Fetch current donor
                val donor = withContext(Dispatchers.IO) {
                    donorRepository.getDonorById(donationItem.donorId)
                }
                _currentDonor.value = donor

                Log.d("DonationViewModel", "Donation details loaded. Fetching check image...")

                // 2. Fetch the image separately
                try {
                    val image = withContext(Dispatchers.IO) {
                       donationRepository.getCheckImageById(donationId)
                    }

                    if (image != null) {
                        Log.d("DonationViewModel", "Check image fetched. Size: ${image.length} chars")
                        _donation.value = donationItem.copy(checkImage = image)
                    } else {
                        Log.d("DonationViewModel", "Check image is null for donation ID: $donationId")
                    }
                } catch (e: Exception) {
                    Log.e("DonationViewModel", "Error fetching check image", e)
                }
            } else {
                Log.e("DonationViewModel", "Donation details not found for ID: $donationId")
            }
        }
    }

    fun loadAllDonors() {
        viewModelScope.launch {
             _donors.value = withContext(Dispatchers.IO) {
                 donorRepository.getAllDonors()
             }
        }
    }

    fun updateDonation(donation: Donation) {
        viewModelScope.launch {
            donationRepository.updateDonation(donation)
            // Refresh donor if it changed
             val donor = withContext(Dispatchers.IO) {
                 donorRepository.getDonorById(donation.donorId)
             }
             _currentDonor.value = donor
        }
    }
    
    fun addAlias(donorId: Long, firstName: String, lastName: String) {
        viewModelScope.launch {
             withContext(Dispatchers.IO) {
                 donorRepository.addAlias(donorId, firstName, lastName)
             }
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
