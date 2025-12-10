package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import javax.inject.Inject
import javax.inject.Singleton

interface CheckImageRepository {
    /**
     * Saves the data for a captured check image to the database.
     *
     * @param donationId The ID of the donation this check is associated with.
     * @param batchId The ID of the batch this check belongs to.
     * @param donorId The optional ID of the donor.
     * @param imageData The URI or path to the stored image file.
     * @return A Result containing the ID of the newly created check image record, or an exception on failure.
     */
    suspend fun saveCheckImage(
        donationId: Long,
        batchId: Long,
        donorId: Long?,
        imageData: String
    ): Result<Long>
}

@Singleton
class CheckImageRepositoryImpl @Inject constructor(
    private val checkImageDao: CheckImageDao // Hilt will inject the DAO
) : CheckImageRepository {

    override suspend fun saveCheckImage(
        donationId: Long,
        batchId: Long,
        donorId: Long?,
        imageData: String
    ): Result<Long> {
        return try {
            // 1. Create the database entity from the function parameters.
            val newCheckImage = CheckImageEntity(
                donationId = donationId,
                batchId = batchId,
                donorId = donorId,
                imageData = imageData
            )

            // 2. Use the injected DAO to insert the entity into the database.
            val newId = checkImageDao.insertCheckImage(newCheckImage)

            // 3. Return the ID of the new record in a success Result.
            Result.success(newId)
        } catch (e: Exception) {
            // If anything goes wrong (e.g., database error), wrap the exception in a failure Result.
            Result.failure(e)
        }
    }
}
