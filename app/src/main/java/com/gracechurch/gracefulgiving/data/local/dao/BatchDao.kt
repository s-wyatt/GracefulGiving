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

    @Insert
    suspend fun insertDonor(donor: DonorEntity): Long

    @Insert
    suspend fun insertDonation(donation: DonationEntity): Long

    @Transaction
    @Query("SELECT * FROM batches WHERE batchId = :id")
    fun getBatchWithDonations(id: Long): Flow<BatchWithDonations?>

    @Query("SELECT * FROM batches ORDER BY createdOn DESC")
    fun getAllBatchesWithDonations(): Flow<List<BatchWithDonations>>

    companion object {
        var allBatchesWithDonations: Flow<List<BatchWithDonations>> = TODO("initialize me")
    }
}
