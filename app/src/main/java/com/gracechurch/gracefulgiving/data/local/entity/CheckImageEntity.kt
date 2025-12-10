package com.gracechurch.gracefulgiving.data.local.entity
import androidx.room.*

@Entity(
    tableName = "check_images",
    foreignKeys = [
        ForeignKey(entity = DonationEntity::class, parentColumns = ["id"], childColumns = ["donationId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BatchEntity::class, parentColumns = ["id"], childColumns = ["batchId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DonorEntity::class, parentColumns = ["id"], childColumns = ["donorId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("donationId"), Index("batchId"), Index("donorId")]
)
data class CheckImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val donationId: Long,
    val batchId: Long,
    val donorId: Long?,
    val imageData: String,
    val capturedAt: Long = System.currentTimeMillis()
)