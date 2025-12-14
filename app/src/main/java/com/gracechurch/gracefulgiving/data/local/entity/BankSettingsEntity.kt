package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_settings")
data class BankSettingsEntity(
    @PrimaryKey val id: Long = 1, // Only one settings record
    val bankName: String = "",
    val accountName: String = "",
    val accountNumber: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
