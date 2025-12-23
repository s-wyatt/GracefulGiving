package com.gracechurch.gracefulgiving.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "aliases",
    foreignKeys = [
        ForeignKey(
            entity = DonorEntity::class,
            parentColumns = ["donorId"],
            childColumns = ["donorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AliasEntity(
    @PrimaryKey(autoGenerate = true)
    val aliasId: Long = 0,
    val donorId: Long,
    val firstName: String,
    val lastName: String
)
