package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Donation
import com.gracechurch.gracefulgiving.ui.dashboard.BatchInfo // Import the data class
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun getAllBatches(): Flow<List<BatchWithDonations>>
    fun getBatch(id: Long): Flow<BatchWithDonations?>
    suspend fun createBatch(userId: Long, createdOn: Long, fundId: Long = 1): Long
    suspend fun deleteBatch(batchId: Long)
    suspend fun closeBatch(batchId: Long)
    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long,
        fundId: Long = 1
    )
    suspend fun deleteDonation(donationId: Long)
    suspend fun updateDonation(donation: Donation)
    suspend fun generateBatchReport(batchId: Long)
    suspend fun generateDepositSlip(batchId: Long)

    // GENTLE FIX: Add the new function required by DashboardViewModel
    // This will return a simple list, not a Flow, as it's a one-time fetch.
    suspend fun getOpenBatches(): List<BatchInfo>
}
