package com.gracechurch.gracefulgiving.data.local.dao
import androidx.room.*
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Transaction // <-- @Transaction is crucial for relationship queries
    @Query("SELECT * FROM donations WHERE donationId = :donationId")
    suspend fun getDonationWithDonor(donationId: Long): DonationWithDonor?

    @Query("SELECT * FROM donations WHERE batchId = :batchId")
    fun getDonationsByBatch(batchId: Long): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE donorId = :donorId AND checkDate >= :startDate AND checkDate <= :endDate")
    fun getDonationsByDonorAndDateRange(donorId: Long, startDate: Long, endDate: Long): Flow<List<DonationEntity>>

    @Query("SELECT SUM(checkAmount) FROM donations WHERE batchId = :batchId")
    suspend fun getBatchTotal(batchId: Long): Double?
    @Transaction
    @Query("SELECT * FROM donations WHERE donorId = :donorId AND checkDate BETWEEN :startDate AND :endDate")
    suspend fun getDonationsForDonorInYear(donorId: Long, startDate: Long, endDate: Long): List<DonationWithDonor>

    @Transaction
    @Query("SELECT * FROM donations WHERE batchId = :batchId")
    suspend fun getDonationsForBatch(batchId: Long): List<DonationWithDonor>

    @Query("SELECT * FROM donations ORDER BY checkDate DESC")
    fun getAllDonations(): Flow<List<DonationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long


    @Query("SELECT SUM(checkAmount) FROM donations WHERE checkDate >= :sinceDate")
    suspend fun getTotalDonationsSince(sinceDate: Long): Double?
    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Delete
    suspend fun deleteDonation(donation: DonationEntity)
}