package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey(autoGenerate = true)
    val fundId: Long = 0,
    val name: String,
    val bankName: String,
    val accountName: String,
    val accountNumber: String
)