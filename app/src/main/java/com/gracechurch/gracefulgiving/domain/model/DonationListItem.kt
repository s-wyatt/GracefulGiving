package com.gracechurch.gracefulgiving.domain.model

import androidx.room.ColumnInfo

data class DonationListItem(
    @ColumnInfo(name = "donationId") val donationId: Long,
    @ColumnInfo(name = "donorId") val donorId: Long,
    @ColumnInfo(name = "checkAmount") val checkAmount: Double,
    @ColumnInfo(name = "checkDate") val checkDate: Long,
    @ColumnInfo(name = "checkNumber") val checkNumber: String,
    @ColumnInfo(name = "batchId") val batchId: Long,
    @ColumnInfo(name = "fundId") val fundId: Long
)
