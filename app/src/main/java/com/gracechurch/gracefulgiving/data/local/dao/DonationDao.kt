package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationExportData
import com.gracechurch.gracefulgiving.data.local.entity.DonationListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT donationId, donorId, checkAmount, checkDate, checkNumber, batchId, fundId FROM donations ORDER BY checkDate DESC")
    fun getAllDonations(): Flow<List<DonationListItem>>

    @Query("SELECT * FROM donations WHERE donationId = :donationId")
    fun getDonationById(donationId: Long): Flow<DonationEntity?>

    @Query("SELECT donationId, donorId, checkAmount, checkDate, checkNumber, batchId, fundId FROM donations WHERE donationId = :donationId")
    fun getDonationListItemById(donationId: Long): Flow<DonationListItem?>

    @Query("SELECT checkImage FROM donations WHERE donationId = :donationId")
    suspend fun getCheckImageById(donationId: Long): String?

    @Query("SELECT donationId, donorId, checkAmount, checkDate, checkNumber, batchId, fundId FROM donations WHERE batchId = :batchId ORDER BY checkDate DESC")
    fun getDonationsByBatch(batchId: Long): Flow<List<DonationListItem>>

    @Query("SELECT donationId, donorId, checkAmount, checkDate, checkNumber, batchId, fundId FROM donations WHERE donorId = :donorId ORDER BY checkDate DESC")
    fun getDonationsByDonor(donorId: Long): Flow<List<DonationListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Query("UPDATE donations SET donorId = :destinationDonorId WHERE donorId = :sourceDonorId")
    suspend fun moveDonations(sourceDonorId: Long, destinationDonorId: Long)

    @Query("DELETE FROM donations WHERE donationId = :donationId")
    suspend fun deleteDonationById(donationId: Long)

    @Query("SELECT SUM(checkAmount) FROM donations WHERE checkDate >= :since")
    suspend fun getTotalDonationsSince(since: Long): Double?

    @Query("SELECT SUM(checkAmount) FROM donations WHERE checkDate >= :startDate AND checkDate <= :endDate")
    suspend fun getTotalBetweenDates(startDate: Long, endDate: Long): Double?

    @Query("""
    SELECT
        d.donationId,
        d.checkNumber,
        d.checkAmount,
        d.checkDate,
        don.firstName as donorFirstName,
        don.lastName as donorLastName,
        f.name as fundName
    FROM donations d
    INNER JOIN donors don ON d.donorId = don.donorId
    INNER JOIN funds f ON d.fundId = f.fundId
    ORDER BY d.checkDate DESC, don.lastName ASC, don.firstName ASC
""")
    suspend fun getAllDonationsWithDetails(): List<DonationExportData>
}