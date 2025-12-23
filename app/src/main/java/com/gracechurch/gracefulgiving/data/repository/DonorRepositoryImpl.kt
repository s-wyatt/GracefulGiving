package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.AliasDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.AliasEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DonorRepositoryImpl @Inject constructor(
    private val donorDao: DonorDao,
    private val aliasDao: AliasDao
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

    override suspend fun deleteDonorById(donorId: Long) {
        donorDao.deleteDonorById(donorId)
    }

    override suspend fun getDonorByName(firstName: String, lastName: String): Donor? {
        return donorDao.findDonorByName(firstName, lastName)?.toDomain()
    }

    override suspend fun createDonor(firstName: String, lastName: String): Long {
        return donorDao.insertDonor(DonorEntity(firstName = firstName, lastName = lastName))
    }

    override suspend fun addAlias(donorId: Long, firstName: String, lastName: String) {
        val alias = AliasEntity(
            donorId = donorId,
            firstName = firstName,
            lastName = lastName
        )
        aliasDao.insertAlias(alias)
    }

    override suspend fun findDonorByAlias(firstName: String, lastName: String): Donor? {
        val alias = aliasDao.findAliasByName(firstName, lastName)
        return alias?.let { donorDao.getDonorById(it.donorId)?.toDomain() }
    }

    override suspend fun findDonorsByAliasLastName(lastName: String): List<Donor> {
        val aliases = aliasDao.findAliasesByLastName("%${lastName}%")
        return aliases.mapNotNull { donorDao.getDonorById(it.donorId)?.toDomain() }
    }
}

private fun DonorEntity.toDomain(): Donor {
    return Donor(
        donorId = this.donorId,
        firstName = this.firstName,
        lastName = this.lastName
    )
}
