package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<Unit>
}
