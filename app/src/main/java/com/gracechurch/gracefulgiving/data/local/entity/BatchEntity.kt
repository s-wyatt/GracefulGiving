package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val batchId: Long = 0,
    val batchNumber: Long,
    val userId: Long,
    val createdOn: Long,
    val status: String = "open"
)
