package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "donations",
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
        ),
        ForeignKey(
            entity = FundEntity::class,
            parentColumns = ["fundId"],
            childColumns = ["fundId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [Index("donorId"), Index("batchId"), Index("fundId")]
)
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val donationId: Long = 0L,
    val donorId: Long,
    val batchId: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val checkDate: Long,
    val checkImage: String? = null,
    val fundId: Long = 1
)
