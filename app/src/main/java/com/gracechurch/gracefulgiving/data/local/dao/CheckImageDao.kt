package com.gracechurch.gracefulgiving.data.local.dao
import androidx.room.*
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckImageDao {
    @Query("SELECT * FROM check_images WHERE donationId = :donationId LIMIT 1")
    suspend fun getImageByDonation(donationId: Long): CheckImageEntity?

    @Query("SELECT * FROM check_images WHERE batchId = :batchId")
    fun getImagesByBatch(batchId: Long): Flow<List<CheckImageEntity>>

    @Query("SELECT * FROM check_images WHERE donorId = :donorId")
    fun getImagesByDonor(donorId: Long): Flow<List<CheckImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckImage(image: CheckImageEntity): Long
}