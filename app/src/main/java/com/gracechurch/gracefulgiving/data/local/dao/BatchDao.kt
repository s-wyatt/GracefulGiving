package com.gracechurch.gracefulgiving.data.local.dao
import BatchWithDonations
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Transaction
    @Query("SELECT * FROM batches WHERE id = :batchId")
    suspend fun getBatchWithDonations(batchId: Long): BatchWithDonations

    @Transaction
    @Query("SELECT * FROM batches")
    suspend fun getAllBatchesWithDonations(): List<BatchWithDonations>

    @Insert
    suspend fun insertDonation(donation: DonationEntity)
    @Query("SELECT * FROM batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Query("SELECT COUNT(*) FROM batches WHERE batchDate LIKE :datePrefix")
    suspend fun countBatchesForDate(datePrefix: String): Long

    @Query("SELECT * FROM batches WHERE batchNumber = :batchNumber")
    suspend fun getBatchByNumber(batchNumber: String): BatchEntity?

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertBatch(batch: BatchEntity): Long

    @Insert
    suspend fun insertBatch(batch: BatchEntity): Long
    @Query("DELETE FROM batches WHERE batchNumber = :batchNumber")
    suspend fun deleteBatch(batchNumber: String): Int

    @Update
    suspend fun updateBatch(batch: BatchEntity)
}