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
        fullName: String,
        password: String,
        role: UserRole,
        tempPassword: String? = null,
        avatarUri: String? = null
    ): Result<Long>

    suspend fun updateUser(user: User): Result<Unit>

    suspend fun updatePassword(
        userId: Long,
        newPassword: String
    ): Result<Unit>

    suspend fun setTemporaryPassword(
        userId: Long,
        tempPassword: String
    ): Result<Unit>

    suspend fun getAllUsers(): List<User>

    suspend fun getUserById(userId: Long): User?

    suspend fun getUserByEmail(email: String): User?

    suspend fun deleteUser(user: User): Result<Unit>
    // Add this method
    suspend fun getCurrentUser(): User?

    // Add these methods to manage current user session
    suspend fun setCurrentUserId(userId: Long)

    suspend fun clearCurrentUser()
}
