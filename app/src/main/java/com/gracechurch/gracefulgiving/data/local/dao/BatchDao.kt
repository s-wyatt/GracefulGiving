package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Query("SELECT COALESCE(MAX(batchNumber), 0) + 1 FROM batches")
    suspend fun getNextBatchNumber(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Transaction
    @Query("SELECT * FROM batches WHERE batchId = :id")
    fun getBatchWithDonations(id: Long): Flow<BatchWithDonations?>

    @Transaction
    @Query("SELECT * FROM batches ORDER BY createdOn DESC")
    fun getAllBatchesWithDonations(): Flow<List<BatchWithDonations>>

    @Query("SELECT MAX(batchNumber) FROM batches")
    suspend fun getMaxBatchNumber(): Long?

    @Query("DELETE FROM batches WHERE batchId = :batchId")
    suspend fun deleteBatch(batchId: Long)

    @Query("UPDATE batches SET status = :status WHERE batchId = :batchId")
    suspend fun updateBatchStatus(batchId: Long, status: String)

    @Query("SELECT * FROM batches ORDER BY batchId")
    suspend fun getAllBatchesList(): List<BatchEntity>
}
