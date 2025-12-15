package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonorWithDonations
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors")
    fun getAllDonorsWithDonations(): Flow<List<DonorWithDonations>>

    @Query("SELECT * FROM donors WHERE donorId = :donorId")
    suspend fun getDonorById(donorId: Long): DonorEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDonor(donor: DonorEntity): Long

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Query("SELECT * FROM donors WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    suspend fun findDonorByName(firstName: String, lastName: String): DonorEntity?
}
