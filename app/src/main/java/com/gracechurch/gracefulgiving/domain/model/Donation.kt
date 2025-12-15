package com.gracechurch.gracefulgiving.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single donation in the domain layer.
 * This is the clean data model that the UI and ViewModels will interact with.
 */
@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val donationId: Long,
    val donorId: Long,
    val checkAmount: Double,
    val checkDate: Long,
    val checkNumber: String,
    val checkImage: String?,
    val batchId: Long,
    val fundId: Long = 1
)