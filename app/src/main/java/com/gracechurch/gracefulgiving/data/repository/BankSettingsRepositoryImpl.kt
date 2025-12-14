package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BankSettingsDao
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.domain.repository.BankSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BankSettingsRepositoryImpl @Inject constructor(
    private val dao: BankSettingsDao
) : BankSettingsRepository {

    override fun getBankSettings(): Flow<BankSettingsEntity?> {
        return dao.getBankSettings()
    }

    override suspend fun saveBankSettings(settings: BankSettingsEntity) {
        dao.saveBankSettings(settings)
    }
}
