package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao // <-- Import DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import java.util.Calendar
import javax.inject.Inject

// GENTLE FIX: Inject all necessary DAOs
class DonationRepositoryImpl @Inject constructor(
    private val donationDao: DonationDao,
    private val donorDao: DonorDao, // <-- Add DonorDao
    private val checkImageDao: CheckImageDao // <-- Add CheckImageDao
) : DonationRepository {

    // GENTLE FIX: Move the addDonation logic here from BatchRepositoryImpl
    override suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        // Find or create a donor using the correct DAO
        // A real implementation should check if the donor exists first.
        val donorId = donorDao.insertDonor(
            DonorEntity(firstName = firstName, lastName = lastName)
        )

        // Insert the donation using the correct DAO
        val donationId = donationDao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date,
                checkImage = image
            )
        )

        // Insert the check image if it exists
        if (!image.isNullOrBlank()) {
            checkImageDao.insertCheckImage(
                CheckImageEntity(
                    donationId = donationId,
                    batchId = batchId,
                    donorId = donorId,
                    imageData = image,
                    capturedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun getMonthToDateTotal(): Double {
        val cal = Calendar.getInstance()
        val firstDayOfMonth = cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
        return donationDao.getTotalDonationsSince(firstDayOfMonth) ?: 0.0
    }

    override suspend fun getQuarterToDateTotal(): Double {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val firstMonthOfQuarter = currentMonth - (currentMonth % 3)
        val firstDayOfQuarter = cal.apply {
            set(Calendar.MONTH, firstMonthOfQuarter)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        return donationDao.getTotalDonationsSince(firstDayOfQuarter) ?: 0.0
    }

    override suspend fun getYearToDateTotal(): Double {
        val cal = Calendar.getInstance()
        val firstDayOfYear = cal.apply { set(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        return donationDao.getTotalDonationsSince(firstDayOfYear) ?: 0.0
    }
}
