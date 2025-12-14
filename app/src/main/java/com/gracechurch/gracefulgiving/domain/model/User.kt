package com.gracechurch.gracefulgiving.domain.model

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val role: UserRole,
    val isTemp: Boolean,
    val createdAt: Long
)
