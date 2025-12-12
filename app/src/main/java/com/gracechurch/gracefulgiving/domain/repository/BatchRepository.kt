package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    // GENTLE FIX: Change this to return a Flow for reactive updates
    fun getAllBatches(): Flow<List<BatchWithDonations>>

    fun getBatch(id: Long): Flow<BatchWithDonations?>
    suspend fun createBatch(userId: Long): Long
    suspend fun deleteBatch(batchId: Long)
    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    )

    suspend fun generateBatchReport(batchId: Long)
    suspend fun generateDepositSlip(batchId: Long)
}
