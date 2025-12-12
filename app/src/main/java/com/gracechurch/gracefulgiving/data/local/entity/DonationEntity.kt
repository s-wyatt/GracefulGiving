package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "donations",
    // GENTLE FIX: Add the foreignKeys block to define relationships
    foreignKeys = [
        ForeignKey(
            entity = DonorEntity::class,
            parentColumns = ["donorId"],
            childColumns = ["donorId"],
            onDelete = ForeignKey.CASCADE // If a donor is deleted, delete their donations
        ),
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["batchId"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE // If a batch is deleted, delete its donations
        )
    ]
)
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val donationId: Long = 0L,
    val donorId: Long,
    val batchId: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val checkDate: Long,
    val checkImage: String? = null
)
