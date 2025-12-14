package com.gracechurch.gracefulgiving.domain.model

/**
 * Represents a single donation in the domain layer.
 * This is the clean data model that the UI and ViewModels will interact with.
 */
data class Donation(
    val donationId: Long,
    val donorId: Long,
    val checkAmount: Double,
    val checkDate: Long,
    val checkNumber: String,
    val checkImage: String?,
    val fundId: Long = 1
)