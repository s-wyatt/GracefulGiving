package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.domain.model.Donor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling all data operations related to Donors.
 * It abstracts the data source (local Room database) from the rest of the application.
 */
@Singleton
class DonorRepository @Inject constructor(
    private val donorDao: DonorDao // Hilt provides the DAO instance
) {

    /**
     * Creates a new donor in the database.
     * @return A Result containing the ID of the new donor, or an exception on failure.
     */
    suspend fun createDonor(firstName: String, lastName: String, optOutStatement: Boolean): Result<Long> {
        return try {
            // Optional: Check if a donor with the same name already exists
            if (donorDao.findDonorByName(firstName, lastName) != null) {
                return Result.failure(Exception("A donor with this name already exists."))
            }

            val donorEntity = DonorEntity(
                firstName = firstName,
                lastName = lastName
//                ,optOutStatement = optOutStatement
            )
            val newId = donorDao.insertDonor(donorEntity)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing donor's information.
     */
    suspend fun updateDonor(donor: Donor): Result<Unit> {
        return try {
            // Map the domain model back to an entity to be saved
            val donorEntity = mapDomainToEntity(donor)
            donorDao.updateDonor(donorEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a single donor by their unique ID.
     * @return A Donor object or null if not found.
     */
    suspend fun getDonorById(id: Long): Donor? {
        return donorDao.getDonorById(id)?.let { entity ->
            mapEntityToDomain(entity)
        }
    }

    /**
     * Fetches all donors from the database, ordered by name.
     * @return A list of Donor objects.
     */
    suspend fun getAllDonors(): List<Donor> {
        return donorDao.getAllDonors().map { entity ->
            mapEntityToDomain(entity)
        }
    }

    /**
     * Searches for donors whose first or last name matches the query.
     * @return A list of matching Donor objects.
     */
    suspend fun searchDonors(query: String): List<Donor> {
        // Add wildcards for a 'contains' search
        val searchQuery = "%$query%"
        return donorDao.searchDonors(searchQuery).map { entity ->
            mapEntityToDomain(entity)
        }
    }

    /**
     * Deletes a donor from the database.
     */
    suspend fun deleteDonor(donor: Donor): Result<Unit> {
        return try {
            val donorEntity = mapDomainToEntity(donor)
            donorDao.deleteDonor(donorEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to map a database DonorEntity to a domain Donor model.
     */
    private fun mapEntityToDomain(donorEntity: DonorEntity): Donor {
        return Donor(
            donorId = donorEntity.donorId,
            firstName = donorEntity.firstName,
            lastName = donorEntity.lastName,
            optOutStatement = donorEntity.optOutStatement,
            createdAt = donorEntity.createdAt
        )
    }

    /**
     * Helper function to map a domain Donor model back to a database DonorEntity.
     */
    private fun mapDomainToEntity(donor: Donor): DonorEntity {
        return DonorEntity(
            donorId = donor.donorId,
            firstName = donor.firstName,
            lastName = donor.lastName,
            optOutStatement = donor.optOutStatement,
            createdAt = donor.createdAt
        )
    }
}
