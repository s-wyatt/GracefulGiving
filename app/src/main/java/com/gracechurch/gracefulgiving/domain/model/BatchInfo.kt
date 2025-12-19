package com.gracechurch.gracefulgiving.domain.model

import java.util.Date

data class BatchInfo(
    val batchId: Long,
    val batchName: String,
    val total: Double,
    val date: Date,
    val fundName: String
)