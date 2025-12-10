package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donations")
data class DonationEntity(
    @PrimaryKey(autoGenerate = true) val donationId: Long = 0,
    val donorId: Long,
    val batchId: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val checkDate: Long,
    val checkImage: String? = null
)
