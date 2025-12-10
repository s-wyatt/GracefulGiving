package com.gracechurch.gracefulgiving.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity

data class BatchWithDonations(
    @Embedded val batch: BatchEntity,

    @Relation(
        entity = DonationEntity::class,
        parentColumn = "batchId",
        entityColumn = "batchId"
    )
    val donations: List<DonationWithDonor>
)
