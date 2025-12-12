package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.*
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Insert
    suspend fun insertBatch(batch: BatchEntity): Long

    @Transaction
    @Query("SELECT * FROM batches WHERE batchId = :id")
    fun getBatchWithDonations(id: Long): Flow<BatchWithDonations?>
    @Transaction
    @Query("SELECT * FROM batches ORDER BY createdOn DESC")
    fun getAllBatchesWithDonations(): Flow<List<BatchWithDonations>>

    // GENTLE FIX: Add the function to get the highest batch number.
    // This is needed by the repository to create a new batch.
    @Query("SELECT MAX(batchNumber) FROM batches")
    suspend fun getMaxBatchNumber(): Long?

    // GENTLE FIX: Add the function to delete a batch by its ID.
    @Query("DELETE FROM batches WHERE batchId = :batchId")
    suspend fun deleteBatch(batchId: Long)
}
