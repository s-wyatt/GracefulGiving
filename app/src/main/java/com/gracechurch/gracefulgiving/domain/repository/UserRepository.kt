package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole

interface UserRepository {

    suspend fun authenticateUser(
        email: String,
        password: String
    ): User?

    suspend fun createUser(
        email: String,
        username: String,
        password: String,
        role: UserRole
    ): Result<Long>

    suspend fun updatePassword(
        userId: Long,
        newPassword: String
    ): Result<Unit>

    suspend fun getAllUsers(): List<User>

    suspend fun getUserById(userId: Long): User?

    suspend fun getUserByEmail(email: String): User?

    suspend fun deleteUser(user: User): Result<Unit>
}
