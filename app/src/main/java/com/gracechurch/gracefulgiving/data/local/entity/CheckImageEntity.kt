package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_images",
    foreignKeys = [
        ForeignKey(
            entity = DonationEntity::class,
            parentColumns = ["donationId"],
            childColumns = ["donationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["batchId"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DonorEntity::class,
            parentColumns = ["donorId"],
            childColumns = ["donorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("donationId"), Index("batchId"), Index("donorId")]
)
data class CheckImageEntity(
    @PrimaryKey(autoGenerate = true) val checkImageId: Long = 0,
    val donationId: Long = 0,
    val batchId: Long,
    val donorId: Long?,
    val imageData: String,
    val capturedAt: Long = System.currentTimeMillis()
)