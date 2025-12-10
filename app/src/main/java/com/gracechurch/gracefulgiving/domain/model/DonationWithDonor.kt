package com.gracechurch.gracefulgiving.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity

data class DonationWithDonor(
    @Embedded
    val donation: Donation,

    @Relation(
        parentColumn = "donorId",
        entityColumn = "id"
    )
    val donor: DonorEntity
)
