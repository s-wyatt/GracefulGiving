package com.gracechurch.gracefulgiving.domain.model

data class CheckImage(
    val id: Long = 0,
    val donationId: Long,
    val batchId: Long,
    val donorId: Long?,
    val imageData: String,
    val capturedAt: Long = System.currentTimeMillis()
)
