package com.gracechurch.gracefulgiving.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo


@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true)
    val donorId: Long = 0L,
    @ColumnInfo(name = "firstName") val firstName: String,
    @ColumnInfo(name = "lastName") val lastName: String
)


