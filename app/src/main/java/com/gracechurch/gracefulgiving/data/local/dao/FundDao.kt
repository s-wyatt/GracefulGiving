package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracechurch.gracefulgiving.data.local.entity.FundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity)

    @Query("SELECT * FROM funds")
    fun getFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE fundId = :fundId")
    suspend fun getFund(fundId: Long): FundEntity?
}