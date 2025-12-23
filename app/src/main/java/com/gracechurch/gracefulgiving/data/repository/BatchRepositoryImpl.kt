package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.FundDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.data.mappers.toDomain
import com.gracechurch.gracefulgiving.domain.model.Batch
import com.gracechurch.gracefulgiving.domain.model.BatchInfo
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject

class BatchRepositoryImpl @Inject constructor(
    private val dao: BatchDao,
    private val fundDao: FundDao,
    private val donationRepo: DonationRepository
) : BatchRepository {

    override fun getAllBatches(): Flow<List<BatchWithDonations>> =
        dao.getAllBatchesWithDonations()

    override fun getBatch(id: Long): Flow<BatchWithDonations?> =
        dao.getBatchWithDonations(id)

    override suspend fun createBatch(userId: Long, createdOn: Long, fundId: Long): Long {
        val nextBatchNumber = (dao.getMaxBatchNumber() ?: 0) + 1
        return dao.insertBatch(
            BatchEntity(
                batchNumber = nextBatchNumber,
                userId = userId,
                createdOn = createdOn,
                fundId = fundId
            )
        )
    }

    override suspend fun deleteBatch(batchId: Long) {
        dao.deleteBatch(batchId)
    }

    override suspend fun closeBatch(batchId: Long) {
        dao.updateBatchStatus(batchId, "closed")
    }

    override suspend fun updateBatch(batch: Batch) {
        dao.updateBatch(
            BatchEntity(
                batchId = batch.batchId,
                batchNumber = batch.batchNumber.toLongOrNull() ?: 0L,
                userId = batch.createdBy,
                createdOn = batch.batchDate,
                status = batch.status,
                fundId = batch.fundId
            )
        )
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
        donationRepo.addDonation(firstName, lastName, checkNumber, amount, date, image, batchId, fundId, donorId)
    }

    override suspend fun deleteDonation(donationId: Long) {
        donationRepo.deleteDonation(donationId)
    }

    override suspend fun updateDonation(donation: Donation) {
        donationRepo.updateDonation(donation)
    }

    override suspend fun getOpenBatches(): List<BatchInfo> = coroutineScope {
        val batchesWithDonations = dao.getAllBatchesWithDonations().first()
        val openBatches = batchesWithDonations.filter { it.batch.status == "open" }
        
        openBatches.map { batchWithDonations ->
            async {
                val fund = fundDao.getFund(batchWithDonations.batch.fundId)
                BatchInfo(
                    batchId = batchWithDonations.batch.batchId,
                    batchName = "Batch #${batchWithDonations.batch.batchNumber}",
                    total = batchWithDonations.donations.sumOf { it.donation.checkAmount },
                    date = Date(batchWithDonations.batch.createdOn),
                    fundName = fund?.name ?: ""
                )
            }
        }.awaitAll()
    }

    override suspend fun generateBatchReport(batchId: Long) {
        // TODO: Implement PDF export here
    }

    override suspend fun generateDepositSlip(batchId: Long) {
        // TODO: Implement PDF export here
    }
}
