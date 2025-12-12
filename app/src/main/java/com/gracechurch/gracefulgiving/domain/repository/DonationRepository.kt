package com.gracechurch.gracefulgiving.domain.repository

interface DonationRepository {
    // GENTLE FIX: Add the new functions required by DashboardViewModel
    suspend fun getMonthToDateTotal(): Double
    suspend fun getQuarterToDateTotal(): Double
    suspend fun getYearToDateTotal(): Double
}
