package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Batch
import com.gracechurch.gracefulgiving.domain.model.BatchInfo
import com.gracechurch.gracefulgiving.domain.model.Donation
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun getAllBatches(): Flow<List<BatchWithDonations>>
    fun getBatch(id: Long): Flow<BatchWithDonations?>
    suspend fun createBatch(userId: Long, createdOn: Long, fundId: Long = 1): Long
    suspend fun deleteBatch(batchId: Long)
    suspend fun closeBatch(batchId: Long)
    suspend fun updateBatch(batch: Batch)
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
    suspend fun deleteDonation(donationId: Long)
    suspend fun updateDonation(donation: Donation)
    suspend fun generateBatchReport(batchId: Long)
    suspend fun generateDepositSlip(batchId: Long)
    suspend fun getOpenBatches(): List<BatchInfo>
}
