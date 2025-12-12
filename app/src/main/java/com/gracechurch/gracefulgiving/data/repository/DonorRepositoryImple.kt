package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonorWithDonations
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DonorRepositoryImpl @Inject constructor(
    private val donorDao: DonorDao,
    private val donationDao: DonationDao
) : DonorRepository {
    override fun getDonor(donorId: Long): Flow<DonorWithDonations?> =
        donorDao.getDonorWithDonations(donorId)

    override fun getAllDonors(): Flow<List<DonorWithDonations>> =
        donorDao.getAllDonorsWithDonations()

    override fun getAllDonations(): Flow<List<DonationEntity>> =
        donationDao.getAllDonations()

    override suspend fun addDonor(firstName: String, lastName: String): Long {
        return donorDao.insertDonor(
            DonorEntity(firstName = firstName, lastName = lastName)
        )
    }

    override suspend fun deleteDonor(donorId: Long) {
        val donor = donorDao.getDonorById(donorId)
        if (donor != null) {
            donorDao.deleteDonor(donor)
        }
    }

    override suspend fun addDonation(
        donorId: Long,
        batchId: Long,
        checkNumber: String,
        amount: Double,
        date: Long
    ) {
        donationDao.insertDonation(
            DonationEntity(
                donorId = donorId,
                batchId = batchId,
                checkNumber = checkNumber,
                checkAmount = amount,
                checkDate = date
            )
        )
    }
}