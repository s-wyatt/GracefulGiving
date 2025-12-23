package com.gracechurch.gracefulgiving.data.mappers

import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationListItem
import com.gracechurch.gracefulgiving.domain.model.Donation

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

// Mapper for DonationEntity -> Domain DonationListItem (keep if needed, or remove if unused)
fun DonationEntity.toDonationListItem(): com.gracechurch.gracefulgiving.domain.model.DonationListItem {
    return com.gracechurch.gracefulgiving.domain.model.DonationListItem(
        donationId = this.donationId,
        donorId = this.donorId,
        checkAmount = this.checkAmount,
        checkDate = this.checkDate,
        checkNumber = this.checkNumber,
        batchId = this.batchId,
        fundId = this.fundId
    )
}

// Mapper for Database DonationListItem -> Domain DonationListItem
fun com.gracechurch.gracefulgiving.data.local.entity.DonationListItem.toDonationListItem(): com.gracechurch.gracefulgiving.domain.model.DonationListItem {
    return com.gracechurch.gracefulgiving.domain.model.DonationListItem(
        donationId = this.donationId,
        donorId = this.donorId,
        checkAmount = this.checkAmount,
        checkDate = this.checkDate,
        checkNumber = this.checkNumber,
        batchId = this.batchId,
        fundId = this.fundId
    )
}

// Mapper for Database DonationListItem -> Domain Donation
fun com.gracechurch.gracefulgiving.data.local.entity.DonationListItem.toDomain(): Donation {
    return Donation(
        donationId = this.donationId,
        donorId = this.donorId,
        checkAmount = this.checkAmount,
        checkDate = this.checkDate,
        checkNumber = this.checkNumber,
        checkImage = null, // Image is not loaded in list item
        batchId = this.batchId,
        fundId = this.fundId
    )
}
