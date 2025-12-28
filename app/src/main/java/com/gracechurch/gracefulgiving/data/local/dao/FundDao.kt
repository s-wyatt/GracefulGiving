package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracechurch.gracefulgiving.data.local.entity.FundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {

    @Query("SELECT * FROM funds WHERE name = :name LIMIT 1")
    suspend fun findFundByName(name: String): FundEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFund(fund: FundEntity): Long

    @Query("SELECT * FROM funds")
    fun getFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE fundId = :fundId")
    suspend fun getFund(fundId: Long): FundEntity?

    @Query("SELECT * FROM funds ORDER BY fundId")
    suspend fun getAllFunds(): List<FundEntity>
}
