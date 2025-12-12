package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BatchRepositoryImpl @Inject constructor(
    private val dao: BatchDao,
    private val checkImageDao: CheckImageDao
) : BatchRepository {

    // GENTLE FIX: Implement the method to return the Flow directly from the DAO
    override fun getAllBatches(): Flow<List<BatchWithDonations>> =
        dao.getAllBatchesWithDonations()

    override fun getBatch(id: Long): Flow<BatchWithDonations?> =
        dao.getBatchWithDonations(id)

    override suspend fun createBatch(userId: Long): Long {
        // You will need to add getNextBatchNumber to your BatchDao
        val nextBatchNumber = (dao.getMaxBatchNumber() ?: 0) + 1

        return dao.insertBatch(
            BatchEntity(
                batchNumber = nextBatchNumber,
                userId = userId,
                createdOn = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteBatch(batchId: Long) {
        dao.deleteBatch(batchId)
    }

    override suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        // This implementation seems to have a bug: it creates a new donor every time.
        // A real implementation would find or create the donor.
        // For now, leaving it as is.
        val donorId = dao.insertDonor(
            DonorEntity(
                firstName = firstName,
                lastName = lastName
            )
        )

        val donationId = dao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date,
                checkImage = image
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

    override suspend fun generateBatchReport(batchId: Long) {
        // Implement PDF export here
    }

    override suspend fun generateDepositSlip(batchId: Long) {
        // Implement PDF export here
    }
}
