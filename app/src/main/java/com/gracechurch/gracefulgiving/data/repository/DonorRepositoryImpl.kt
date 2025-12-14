package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.domain.model.Donor
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DonorRepositoryImpl @Inject constructor(
    private val donorDao: DonorDao
) : DonorRepository {
    override suspend fun getAllDonors(): List<Donor> {
        return donorDao.getAllDonorsWithDonations().first().map { donorWithDonations ->
            Donor(
                donorId = donorWithDonations.donor.donorId,
                firstName = donorWithDonations.donor.firstName,
                lastName = donorWithDonations.donor.lastName
            )
        }
    }
}