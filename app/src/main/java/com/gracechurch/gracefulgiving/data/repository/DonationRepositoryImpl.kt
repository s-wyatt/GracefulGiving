package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.mappers.toDomain
import com.gracechurch.gracefulgiving.data.mappers.toEntity
import com.gracechurch.gracefulgiving.data.mappers.toDonationListItem
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.model.DonationListItem
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

    override fun getAllDonations(): Flow<List<DonationListItem>> {
        return donationDao.getAllDonations().map { entities ->
            entities.map { it.toDonationListItem() }
        }
    }

    override fun getDonationsByDonor(donorId: Long): Flow<List<DonationListItem>> {
        return donationDao.getDonationsByDonor(donorId).map { entities ->
            entities.map { it.toDonationListItem() }
        }
    }

    override suspend fun getCheckImageById(donationId: Long): String? {
        return donationDao.getCheckImageById(donationId)
    }

    override suspend fun getDonationById(id: Long): Donation? {
        val donationListItem = donationDao.getDonationListItemById(id).firstOrNull()
        return donationListItem?.toDomain()
    }

    override suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long,
        fundId: Long,
        donorId: Long?
    ) {
        val finalDonorId: Long

        if (donorId != null) {
            finalDonorId = donorId
        } else {
            var newDonorId = donorDao.insertDonor(
                DonorEntity(firstName = firstName, lastName = lastName)
            )

            if (newDonorId == -1L) {
                val existingDonor = donorDao.findDonorByName(firstName, lastName)
                if (existingDonor != null) {
                    newDonorId = existingDonor.donorId
                } else {
                    throw IllegalStateException("Could not find or create donor: $firstName $lastName")
                }
            }
            finalDonorId = newDonorId
        }

        val donationId = donationDao.insertDonation(
            DonationEntity(
                donorId = finalDonorId,
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
                    donorId = finalDonorId,
                    imageData = image,
                    capturedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun moveDonations(sourceDonorId: Long, destinationDonorId: Long) {
        donationDao.moveDonations(sourceDonorId, destinationDonorId)
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
