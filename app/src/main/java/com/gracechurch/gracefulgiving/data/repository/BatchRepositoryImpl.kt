package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
// import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao // No longer needed here
// import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity // No longer needed
// import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity // No longer needed
// import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity // No longer needed
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository // <-- Import DonationRepository
import com.gracechurch.gracefulgiving.ui.dashboard.BatchInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// GENTLE FIX: Simplify the constructor
class BatchRepositoryImpl @Inject constructor(
    private val dao: BatchDao,
    private val donationRepo: DonationRepository // <-- Inject the repository, not the DAOs
) : BatchRepository {

    override fun getAllBatches(): Flow<List<BatchWithDonations>> =
        dao.getAllBatchesWithDonations()

    override fun getBatch(id: Long): Flow<BatchWithDonations?> =
        dao.getBatchWithDonations(id)

    override suspend fun createBatch(userId: Long): Long {
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

    // GENTLE FIX: Delegate the addDonation call to the DonationRepository
    override suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        donationRepo.addDonation(firstName, lastName, checkNumber, amount, date, image, batchId)
    }

    override suspend fun getOpenBatches(): List<BatchInfo> {
        return dao.getAllBatchesWithDonations().first().map { batchWithDonations ->
            BatchInfo(
                batchId = batchWithDonations.batch.batchId,
                batchName = "Batch #${batchWithDonations.batch.batchNumber}",
                total = batchWithDonations.donations.sumOf { it.donation.checkAmount }
            )
        }
    }

    override suspend fun generateBatchReport(batchId: Long) {
        // TODO: Implement PDF export here
    }

    override suspend fun generateDepositSlip(batchId: Long) {
        // TODO: Implement PDF export here
    }
}
