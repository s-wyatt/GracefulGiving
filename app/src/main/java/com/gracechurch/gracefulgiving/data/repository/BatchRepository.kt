package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BatchRepository @Inject constructor(
    private val dao: BatchDao,
    private val checkImageDao: CheckImageDao
) {

    fun getBatch(id: Long): Flow<BatchWithDonations?> =
        dao.getBatchWithDonations(id)

    suspend fun createBatch(batchNumber: Long, userId: Long): Long {
        return dao.insertBatch(
            BatchEntity(
                batchNumber = batchNumber,
                userId = userId,
                createdOn = System.currentTimeMillis()
            )
        )
    }

    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        // Insert or get donor
        val donorId = dao.insertDonor(
            DonorEntity(
                firstName = firstName,
                lastName = lastName
            )
        )

        // Insert donation
        val donationId = dao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date,
                checkImage = image  // Keep this if you want it in DonationEntity too
            )
        )

        // Save check image to check_images table if present
        if (!image.isNullOrBlank() && image != "base64mockimage") {
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
}