package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BankSettingsDao
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.domain.repository.BankSettingsRepository
import javax.inject.Inject

class BankSettingsRepositoryImpl @Inject constructor(
    private val dao: BankSettingsDao
) : BankSettingsRepository {

    override suspend fun getBankSettings(): BankSettingsEntity? {
        return dao.getBankSettings()
    }

    override suspend fun saveBankSettings(settings: BankSettingsEntity) {
        dao.saveBankSettings(settings)
    }
}
