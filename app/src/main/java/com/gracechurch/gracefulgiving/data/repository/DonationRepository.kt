package com.gracechurch.gracefulgiving.domain.repository

interface DonationRepository {
    // GENTLE FIX: Add the function signature here
    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    )

    suspend fun getMonthToDateTotal(): Double
    suspend fun getQuarterToDateTotal(): Double
    suspend fun getYearToDateTotal(): Double
}
