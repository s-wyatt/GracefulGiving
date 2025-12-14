package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import kotlinx.coroutines.flow.Flow

interface BankSettingsRepository {
    fun getBankSettings(): Flow<BankSettingsEntity?>
    suspend fun saveBankSettings(settings: BankSettingsEntity)
}
