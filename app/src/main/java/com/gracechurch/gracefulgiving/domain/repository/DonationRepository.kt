package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.Donation
import kotlinx.coroutines.flow.Flow

interface DonationRepository {
    // GENTLE FIX: Return a Flow to allow reactive updates in the ViewModel.
    fun getAllDonations(): Flow<List<Donation>>
    fun getDonationsByDonor(donorId: Long): Flow<List<Donation>>

    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long,
        fundId: Long = 1
    )

    suspend fun deleteDonation(donationId: Long)
    suspend fun updateDonation(donation: Donation)
    suspend fun getDonationById(id: Long): Donation?

    suspend fun getMonthToDateTotal(): Double
    suspend fun getQuarterToDateTotal(): Double
    suspend fun getYearToDateTotal(): Double
    suspend fun getTotalBetweenDates(startDate: Long, endDate: Long): Double
}
