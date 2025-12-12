package com.gracechurch.gracefulgiving.data.local.relations


import androidx.room.Embedded
import androidx.room.Relation
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity


data class DonorWithDonations(
    @Embedded val donor: DonorEntity,
    @Relation(
        parentColumn = "donorId",
        entityColumn = "donorId"
    )
    val donations: List<DonationEntity>
)