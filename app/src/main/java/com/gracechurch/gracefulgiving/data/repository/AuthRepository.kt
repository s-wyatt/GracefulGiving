package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.util.PasswordUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val userDao: UserDao) {
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByUsername(username)
                ?: return Result.failure(Exception("Invalid credentials"))

            // Check if using temporary password
            val isValidPassword = if (userEntity.isTemp && userEntity.tempPassword != null) {
                // For temporary passwords, compare directly (not hashed)
                password == userEntity.tempPassword
            } else {
                // For permanent passwords, verify hash
                PasswordUtils.verifyPassword(password, userEntity.passwordHash)
            }

            if (!isValidPassword) {
                return Result.failure(Exception("Invalid credentials"))
            }

            Result.success(mapEntityToDomain(userEntity))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapEntityToDomain(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            email = userEntity.email,
            username = userEntity.username,
            passwordHash = "",
            role = userEntity.role,
            tempPassword = userEntity.tempPassword,
            isTemp = userEntity.isTemp,
            createdAt = userEntity.createdAt
        )
    }

    suspend fun createUser(
        email: String,
        username: String,
        password: String,
        role: UserRole,
        createdBy: Long,
        isTemporary: Boolean = false
    ): Result<Long> {
        return try {
            if (userDao.getUserByUsername(username) != null) {
                return Result.failure(Exception("User exists"))
            }

            val id = userDao.insertUser(
                UserEntity(
                    email = email,
                    username = username,
                    passwordHash = PasswordUtils.hashPassword(password),
                    role = role,
                    tempPassword = if (isTemporary) password else null,
                    isTemp = isTemporary,
                    createdBy = createdBy
                )
            )

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}