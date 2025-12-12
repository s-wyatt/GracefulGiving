package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.*
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankSettingsDao {
    @Query("SELECT * FROM bank_settings WHERE id = 1 LIMIT 1")
    fun getBankSettings(): Flow<BankSettingsEntity?>

    @Query("SELECT * FROM bank_settings WHERE id = 1 LIMIT 1")
    suspend fun getBankSettingsOnce(): BankSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBankSettings(settings: BankSettingsEntity)

    @Query("DELETE FROM bank_settings")
    suspend fun clearBankSettings()
}