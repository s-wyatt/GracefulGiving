package com.gracechurch.gracefulgiving.domain.model

data class Donation(
    val id: Long = 0,
    val batchId: Long,
    val donorId: Long,
    val checkDate: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
