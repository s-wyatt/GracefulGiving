package com.gracechurch.gracefulgiving.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity

data class DonationWithDonor(
    @Embedded
    val donation: DonationEntity,

    @Relation(
        parentColumn = "donorId",
        entityColumn = "donorId"
    )
    val donor: DonorEntity
)
