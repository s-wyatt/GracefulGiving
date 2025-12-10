package com.gracechurch.gracefulgiving.domain.model

data class Donor(
    val donorId: Long = 0,
    val firstName: String,
    val lastName: String,
    val optOutStatement: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
val Donor.fullName: String
    get() = "$firstName $lastName"

