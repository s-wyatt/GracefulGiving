package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.DonationListItem
import kotlinx.coroutines.flow.Flow

interface DonationRepository {
    fun getAllDonations(): Flow<List<DonationListItem>>
    fun getDonationsByDonor(donorId: Long): Flow<List<DonationListItem>>
    suspend fun getCheckImageById(donationId: Long): String?

    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long,
        fundId: Long = 1,
        donorId: Long? = null
    )

    suspend fun moveDonations(sourceDonorId: Long, destinationDonorId: Long)
    suspend fun deleteDonation(donationId: Long)
    suspend fun updateDonation(donation: Donation)
    suspend fun getDonationById(id: Long): Donation?

    suspend fun getMonthToDateTotal(): Double
    suspend fun getQuarterToDateTotal(): Double
    suspend fun getYearToDateTotal(): Double
    suspend fun getTotalBetweenDates(startDate: Long, endDate: Long): Double
}
