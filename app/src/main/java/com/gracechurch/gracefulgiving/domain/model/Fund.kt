package com.gracechurch.gracefulgiving.domain.model

data class Fund(
    val fundId: Long? = null,
    val name: String,
    val bankName: String,
    val accountName: String,
    val accountNumber: String
)