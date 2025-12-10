package com.gracechurch.gracefulgiving.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class User(
    val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    val tempPassword: String? = null,
    val isTemp: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN, USER
}
