package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.Fund
import kotlinx.coroutines.flow.Flow

interface FundRepository {
    suspend fun insertFund(fund: Fund)
    fun getFunds(): Flow<List<Fund>>
    suspend fun getFund(fundId: Long): Fund?
}