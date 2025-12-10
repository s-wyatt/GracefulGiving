package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import kotlinx.coroutines.flow.Flow

class BatchRepository(private val dao: BatchDao) {

    fun getBatch(id: Long): Flow<BatchWithDonations?> =
        dao.getBatchWithDonations(id)

    suspend fun createBatch(batchNumber: Long, userId: Long): Long {
        return dao.insertBatch(
            BatchEntity(
                batchNumber = batchNumber,
                userId = userId,
                createdOn = System.currentTimeMillis()
            )
        )
    }

    suspend fun addDonation(
        firstName: String,
        lastName: String,
        checkNumber: String,
        amount: Double,
        date: Long,
        image: String?,
        batchId: Long
    ) {
        val donorId = dao.insertDonor(
            DonorEntity(
                firstName = firstName,
                lastName = lastName
            )
        )

        dao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date,
                checkImage = image
            )
        )
    }
}
