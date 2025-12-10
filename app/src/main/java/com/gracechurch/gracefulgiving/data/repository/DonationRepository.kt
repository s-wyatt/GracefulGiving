package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.domain.model.DonationWithDonor
import java.util.*

class DonationRepository(private val donationDao: DonationDao) {

    suspend fun createDonation(
        batchId: Long,
        donorId: Long,
        checkDate: Long,
        checkNumber: String,
        checkAmount: Double
    ): Long {
        val donation = DonationEntity(
            batchId = batchId,
            donorId = donorId,
            checkDate = checkDate,
            checkNumber = checkNumber.trim(),
            checkAmount = checkAmount
        )
        return donationDao.insertDonation(donation)
    }

    suspend fun getDonationsForBatch(batchId: Long): List<DonationWithDonor> {
        return donationDao.getDonationsForBatch(batchId)
    }

    suspend fun getDonationsForDonorInYear(donorId: Long, year: Int): List<DonationWithDonor> {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.timeInMillis

        return donationDao.getDonationsForDonorInYear(donorId, startDate, endDate)
    }

    suspend fun getTotalForBatch(batchId: Long): Double {
        return donationDao.getBatchTotal(batchId) ?: 0.0
    }

    suspend fun getTotalForDonorInYear(donorId: Long, year: Int): Double {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.timeInMillis

        return donationDao.getDonationsForDonorInYear(donorId, startDate, endDate)
            .sumOf { it.donation.checkAmount }
    }

    suspend fun updateDonation(donation: DonationEntity) {
        donationDao.updateDonation(donation)
    }

    suspend fun deleteDonation(donation: DonationEntity) {
        donationDao.deleteDonation(donation)
    }
}
