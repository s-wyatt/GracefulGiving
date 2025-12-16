package com.gracechurch.gracefulgiving.domain.model

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val fullName: String,
    val avatarUri: String? = null,
    val role: UserRole,
    val isTemp: Boolean,
    val createdAt: Long
)
