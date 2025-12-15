package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.Donor

interface DonorRepository {
    suspend fun getAllDonors(): List<Donor>
    suspend fun getDonorById(donorId: Long): Donor?
    suspend fun updateDonor(donor: Donor)
}