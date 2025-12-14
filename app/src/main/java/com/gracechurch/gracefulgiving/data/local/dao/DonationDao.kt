package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations ORDER BY checkDate DESC")
    fun getAllDonations(): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE donationId = :donationId")
    fun getDonationById(donationId: Long): Flow<DonationEntity?>

    @Query("SELECT * FROM donations WHERE batchId = :batchId ORDER BY checkDate DESC")
    fun getDonationsByBatch(batchId: Long): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE donorId = :donorId ORDER BY checkDate DESC")
    fun getDonationsByDonor(donorId: Long): Flow<List<DonationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Query("DELETE FROM donations WHERE donationId = :donationId")
    suspend fun deleteDonationById(donationId: Long)

    @Query("SELECT SUM(checkAmount) FROM donations WHERE checkDate >= :since")
    suspend fun getTotalDonationsSince(since: Long): Double?
    @Query("SELECT SUM(checkAmount) FROM donations WHERE checkDate >= :startDate AND checkDate <= :endDate")
    suspend fun getTotalBetweenDates(startDate: Long, endDate: Long): Double?
}