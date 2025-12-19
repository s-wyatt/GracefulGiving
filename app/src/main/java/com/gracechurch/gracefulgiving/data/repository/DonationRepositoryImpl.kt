package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.mappers.toDomain
import com.gracechurch.gracefulgiving.data.mappers.toEntity
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class DonationRepositoryImpl @Inject constructor(
    private val donationDao: DonationDao,
    private val donorDao: DonorDao,
    private val checkImageDao: CheckImageDao
) : DonationRepository {

    override fun getAllDonations(): Flow<List<Donation>> { // Return Flow<List<Donation>>
        return donationDao.getAllDonations().map { entities ->
            entities.map { it.toDomain() } // <-- Use the extension function
        }
    }


    override fun getDonationsByDonor(donorId: Long): Flow<List<Donation>> { // Return Flow<List<Donation>>
        return donationDao.getDonationsByDonor(donorId).map { entities ->
            entities.map { it.toDomain() } // <-- Use the extension function
        }
    }
    override suspend fun getDonationById(id: Long): Donation? { // Return Donation?
        // Collect the first item from the Flow and map it.
        val donationEntity = donationDao.getDonationById(id).firstOrNull()
        return donationEntity?.toDomain() // <-- Use the extension function
    }

    override suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long,
        fundId: Long
    ) {
        // Check if donor exists or create new
        var donorId = donorDao.insertDonor(
            DonorEntity(firstName = firstName, lastName = lastName)
        )
        
        // If insert returned -1, it means the donor already exists (OnConflictStrategy.IGNORE)
        if (donorId == -1L) {
            val existingDonor = donorDao.findDonorByName(firstName, lastName)
            if (existingDonor != null) {
                donorId = existingDonor.donorId
            } else {
                // This shouldn't typically happen if conflict occurred, but handle gracefully
                throw IllegalStateException("Could not find or create donor: $firstName $lastName")
            }
        }

        val donationId = donationDao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date,
                checkImage = image,
                fundId = fundId
            )
        )

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

    override suspend fun deleteDonation(donationId: Long) {
        donationDao.deleteDonationById(donationId)
    }

    override suspend fun updateDonation(donation: Donation) {
        donationDao.updateDonation(donation.toEntity())
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
    override suspend fun getTotalBetweenDates(startDate: Long, endDate: Long): Double {
        return donationDao.getTotalBetweenDates(startDate, endDate) ?: 0.0
    }

}