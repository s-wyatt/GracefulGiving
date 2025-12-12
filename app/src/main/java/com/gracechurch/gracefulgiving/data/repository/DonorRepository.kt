package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonorWithDonations
import kotlinx.coroutines.flow.Flow

interface DonorRepository {
    fun getDonor(donorId: Long): Flow<DonorWithDonations?>
    fun getAllDonors(): Flow<List<DonorWithDonations>>
    fun getAllDonations(): Flow<List<DonationEntity>>
    suspend fun addDonor(firstName: String, lastName: String): Long
    suspend fun deleteDonor(donorId: Long)
    suspend fun addDonation(donorId: Long, batchId: Long, checkNumber: String, amount: Double, date: Long)
}