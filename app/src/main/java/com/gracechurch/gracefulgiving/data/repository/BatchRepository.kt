package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.domain.model.Batch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRepository @Inject constructor(
    private val batchDao: BatchDao
) {

    /**
     * Creates a new batch in the database.
     * @return A Result containing the ID of the new batch, or an exception on failure.
     */
    suspend fun createBatch(batchDate: Long, createdBy: Long): Result<Long> {
        return try {
            val batchNumber = generateBatchNumber(batchDate)

            // 1. Create the ENTITY to be inserted into the database
            val batchEntity = BatchEntity(
                batchNumber = batchNumber,
                batchDate = batchDate,
                createdBy = createdBy
            )

            // 2. Insert the ENTITY using the DAO
            val id = batchDao.insertBatch(batchEntity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a unique batch number for a given date (e.g., "20251208-001").
     */
    private suspend fun generateBatchNumber(date: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val datePrefix = dateFormat.format(Date(date))

        // This DAO call is missing, so we need to add it to BatchDao.kt
        val count = batchDao.countBatchesForDate(datePrefix)
        return "$datePrefix-${String.format("%03d", count + 1)}"
    }

    /**
     * Fetches all batches as a Flow, automatically mapping them to the domain model.
     */
    fun getAllBatches(): Flow<List<Batch>> {
        return batchDao.getAllBatches().map { entityList ->
            entityList.map { entity -> mapEntityToDomain(entity) }
        }
    }

    /**
     * Fetches a single batch by its ID.
     */
    suspend fun getBatchById(batchId: Long): Batch? {
        return batchDao.getBatchById(batchId)?.let { entity ->
            mapEntityToDomain(entity)
        }
    }

    /**
     * Deletes a batch from the database.
     */
    suspend fun deleteBatch(batchNumber: String): Result<Unit> {
        return try {
            batchDao.deleteBatch(batchNumber)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to map a database BatchEntity to a domain Batch model.
     */
    private fun mapEntityToDomain(entity: BatchEntity): Batch {
        return Batch(
            batchId = entity.id,
            batchNumber = entity.batchNumber,
            batchDate = entity.batchDate,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            status = entity.status
        )
    }

    /**
     * Helper function to map a domain Batch model back to a database BatchEntity.
     */
    private fun mapDomainToEntity(batch: Batch): BatchEntity {
        return BatchEntity(
            id = batch.batchId,
            batchNumber = batch.batchNumber,
            batchDate = batch.batchDate,
            createdBy = batch.createdBy,
            createdAt = batch.createdAt,
            status = batch.status
        )
    }
}
