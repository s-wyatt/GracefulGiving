package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.domain.model.Donation
import kotlinx.coroutines.flow.Flow

interface DonationRepository {
    fun getAllDonations(): Flow<List<Donation>>
    fun getDonationsByDonor(donorId: Long): Flow<List<Donation>>
    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    )
    suspend fun deleteDonation(donationId: Long)
    suspend fun updateDonation(donation: DonationEntity)

    suspend fun getMonthToDateTotal(): Double
    suspend fun getQuarterToDateTotal(): Double
    suspend fun getYearToDateTotal(): Double
}