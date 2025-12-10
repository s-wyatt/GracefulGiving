package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity

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

//    @Query("SELECT * FROM donors WHERE optOutStatement = 0 ORDER BY lastName, firstName")
//    suspend fun getDonorsOptedIn(): List<DonorEntity>

    @Insert
    suspend fun insertDonor(donor: DonorEntity): Long

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Delete
    suspend fun deleteDonor(donor: DonorEntity)
}
