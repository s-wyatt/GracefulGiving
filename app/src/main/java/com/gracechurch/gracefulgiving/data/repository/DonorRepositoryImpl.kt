package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DonorRepositoryImpl @Inject constructor(
    private val donorDao: DonorDao
) : DonorRepository {
    override suspend fun getAllDonors(): List<Donor> {
        return donorDao.getAllDonorsWithDonations().first().map { donorWithDonations ->
            donorWithDonations.donor.toDomain()
        }
    }

    override suspend fun getDonorById(donorId: Long): Donor? {
        return donorDao.getDonorById(donorId)?.toDomain()
    }

    override suspend fun updateDonor(donor: Donor) {
        val existingEntity = donorDao.getDonorById(donor.donorId)
        if (existingEntity != null) {
            val updatedEntity = existingEntity.copy(
                firstName = donor.firstName,
                lastName = donor.lastName
            )
            donorDao.updateDonor(updatedEntity)
        }
    }
}

private fun DonorEntity.toDomain(): Donor {
    return Donor(
        donorId = this.donorId,
        firstName = this.firstName,
        lastName = this.lastName
    )
}
