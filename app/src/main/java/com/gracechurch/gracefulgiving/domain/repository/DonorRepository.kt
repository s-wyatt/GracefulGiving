package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.Donor

interface DonorRepository {
    suspend fun getAllDonors(): List<Donor>
    suspend fun getDonorById(donorId: Long): Donor?
    suspend fun updateDonor(donor: Donor)
    suspend fun deleteDonorById(donorId: Long)
    suspend fun getDonorByName(firstName: String, lastName: String): Donor?
    suspend fun createDonor(firstName: String, lastName: String): Long
    suspend fun addAlias(donorId: Long, firstName: String, lastName: String)
    suspend fun findDonorByAlias(firstName: String, lastName: String): Donor?
    suspend fun findDonorsByAliasLastName(lastName: String): List<Donor>
}