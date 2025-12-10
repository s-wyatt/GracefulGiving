package com.gracechurch.gracefulgiving.data.local.entity
import androidx.room.*

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val optOutStatement: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)