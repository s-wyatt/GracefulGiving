package com.gracechurch.gracefulgiving.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


data class Batch(
    val batchId: Long = 0,
    val batchNumber: String,
    val batchDate: Long,
    val createdBy: Long,
    val createdOn: Long = System.currentTimeMillis(),
    val status: String = "open"
)
