package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BankSettingsDao
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankSettingsRepository @Inject constructor(
    private val dao: BankSettingsDao
) {
    fun getBankSettings(): Flow<BankSettingsEntity?> = dao.getBankSettings()

    suspend fun getBankSettingsOnce(): BankSettingsEntity? = dao.getBankSettingsOnce()

    suspend fun saveBankSettings(
        bankName: String,
        accountName: String,
        accountNumber: String,
        routingNumber: String
    ) {
        dao.insertOrUpdateBankSettings(
            BankSettingsEntity(
                id = 1,
                bankName = bankName,
                accountName = accountName,
                accountNumber = accountNumber,
                routingNumber = routingNumber,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearBankSettings() {
        dao.clearBankSettings()
    }
}