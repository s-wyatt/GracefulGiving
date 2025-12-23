package com.gracechurch.gracefulgiving.data.local.entity

data class DonationExportData(
    val donationId: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val checkDate: Long,
    val donorFirstName: String,
    val donorLastName: String,
    val fundName: String
)