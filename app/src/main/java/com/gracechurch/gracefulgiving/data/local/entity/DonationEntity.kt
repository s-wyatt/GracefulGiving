package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "donations",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DonorEntity::class,
            parentColumns = ["id"],
            childColumns = ["donorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long,
    val donorId: Long,
    val checkDate: Long,
    val checkNumber: String,
    val checkAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
