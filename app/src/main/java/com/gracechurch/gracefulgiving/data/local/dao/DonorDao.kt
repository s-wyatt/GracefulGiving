package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity

import com.gracechurch.gracefulgiving.data.local.relations.DonorWithDonations
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors WHERE donorId = :donorId")
    suspend fun getDonorById(donorId: Long): DonorEntity?

    @Query("SELECT * FROM donors WHERE firstName LIKE :query OR lastName LIKE :query ORDER BY lastName, firstName")
    suspend fun searchDonors(query: String): List<DonorEntity>

    @Query("SELECT * FROM donors WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    suspend fun findDonorByName(firstName: String, lastName: String): DonorEntity?

    @Query("SELECT * FROM donors ORDER BY lastName, firstName")
    suspend fun getAllDonors(): List<DonorEntity>

   @Query("SELECT * FROM donors WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    suspend fun getDonorByName(firstName: String, lastName: String): DonorEntity?

    @Transaction
    @Query("SELECT * FROM donors WHERE donorId = :donorId")
    fun getDonorWithDonations(donorId: Long): Flow<DonorWithDonations?>


    @Transaction
    @Query("SELECT * FROM donors")
    fun getAllDonorsWithDonations(): Flow<List<DonorWithDonations>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity): Long



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Delete
    suspend fun deleteDonor(donor: DonorEntity)
}
