package com.gracechurch.gracefulgiving.data.local.entity
import androidx.room.*

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchNumber: String,
    val batchDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: Long,
    val status: String = "open"
)