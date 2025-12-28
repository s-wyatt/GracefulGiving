package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracechurch.gracefulgiving.data.local.entity.AliasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasDao {
    @Query("SELECT * FROM aliases WHERE donorId = :donorId")
    fun getAliasesForDonor(donorId: Long): Flow<List<AliasEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: AliasEntity): Long

    @Query("SELECT * FROM aliases WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    suspend fun findAliasByName(firstName: String, lastName: String): AliasEntity?

    @Query("SELECT * FROM aliases WHERE lastName LIKE :lastName")
    suspend fun findAliasesByLastName(lastName: String): List<AliasEntity>

    @Query("SELECT * FROM aliases ORDER BY aliasId")
    suspend fun getAllAliases(): List<AliasEntity>
}
