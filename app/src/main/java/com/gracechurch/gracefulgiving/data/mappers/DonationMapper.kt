package com.gracechurch.gracefulgiving.data.mappers

import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.domain.model.Donation

/**
 * Converts a DonationEntity from the data layer to a Donation model in the domain layer.
 */
fun DonationEntity.toDomain(): Donation {
    return Donation(
        donationId = this.donationId,
        donorId = this.donorId,
        checkAmount = this.checkAmount,
        checkDate = this.checkDate,
        checkNumber = this.checkNumber,
        checkImage = this.checkImage,
        batchId = this.batchId,
        fundId = this.fundId
    )
}

/**
 * Converts a Donation from the domain layer to a DonationEntity in the data layer.
 */
fun Donation.toEntity(): DonationEntity {
    return DonationEntity(
        donationId = this.donationId,
        donorId = this.donorId,
        checkAmount = this.checkAmount,
        checkDate = this.checkDate,
        checkNumber = this.checkNumber,
        checkImage = this.checkImage,
        batchId = this.batchId,
        fundId = this.fundId
    )
}
