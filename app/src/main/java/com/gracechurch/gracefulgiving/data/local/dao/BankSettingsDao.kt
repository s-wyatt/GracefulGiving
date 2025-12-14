package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankSettingsDao {

    @Query("SELECT * FROM bank_settings LIMIT 1")
    fun getBankSettings(): Flow<BankSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBankSettings(settings: BankSettingsEntity)
}
