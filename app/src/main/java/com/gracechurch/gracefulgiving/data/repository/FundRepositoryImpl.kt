package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.FundDao
import com.gracechurch.gracefulgiving.data.local.entity.FundEntity
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FundRepositoryImpl @Inject constructor(
    private val fundDao: FundDao
) : FundRepository {

    override suspend fun insertFund(fund: Fund) {
        fundDao.insertFund(fund.toEntity())
    }

    override fun getFunds(): Flow<List<Fund>> {
        return fundDao.getFunds().map {
            it.map { fundEntity -> fundEntity.toModel() }
        }
    }

    override suspend fun getFund(fundId: Long): Fund? {
        return fundDao.getFund(fundId)?.toModel()
    }
    
    override suspend fun getAllFundsOneShot(): List<Fund> {
        return fundDao.getFunds().first().map { it.toModel() }
    }
}

fun Fund.toEntity(): FundEntity {
    return FundEntity(
        fundId = this.fundId ?: 0,
        name = this.name,
        bankName = this.bankName,
        accountName = this.accountName,
        accountNumber = this.accountNumber
    )
}

fun FundEntity.toModel(): Fund {
    return Fund(
        fundId = this.fundId,
        name = this.name,
        bankName = this.bankName,
        accountName = this.accountName,
        accountNumber = this.accountNumber
    )
}