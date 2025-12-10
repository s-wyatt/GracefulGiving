package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.util.PasswordUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Authenticates a user with their email and password.
     * Returns a User domain model on success, or null on failure.
     */
    suspend fun authenticateUser(email: String, password: String): User? {
        val userEntity = userDao.getUserByEmail(email) ?: return null

        return if (PasswordUtils.verifyPassword(password, userEntity.passwordHash)) {
            // Map UserEntity to User on success
            mapEntityToDomain(userEntity)
        } else {
            null
        }
    }

    /**
     * Creates a new user in the database.
     * @return A Result containing the ID of the new user, or an exception on failure.
     */
    suspend fun createUser(
        email: String,
        username: String,
        password: String,
        role: UserRole
    ): Result<Long> {
        return try {
            // Check for existing user to provide a better error message
            if (userDao.getUserByUsername(username) != null) {
                return Result.failure(Exception("Username already exists."))
            }
            if (userDao.getUserByEmail(email) != null) {
                return Result.failure(Exception("An account with this email already exists."))
            }

            val passwordHash = PasswordUtils.hashPassword(password)
            val userEntity = UserEntity(
                email = email,
                username = username,
                passwordHash = passwordHash,
                role = role,
                isTemp = false // A new user does not have a temporary password
            )
            val newId = userDao.insertUser(userEntity)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a user's password.
     */
    suspend fun updatePassword(userId: Long, newPassword: String): Result<Unit> {
        return try {
            val userEntity = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
            val updatedUserEntity = userEntity.copy(
                passwordHash = PasswordUtils.hashPassword(newPassword),
                isTemp = false // The password is now permanent
            )
            userDao.updateUser(updatedUserEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a list of all users.
     */
    suspend fun getAllUsers(): List<User> {
        // Fetch all entities and map each one to a domain model
        return userDao.getAllUsers().map { userEntity ->
            mapEntityToDomain(userEntity)
        }
    }

    /**
     * Fetches a single user by their ID.
     */
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)?.let { userEntity ->
            mapEntityToDomain(userEntity)
        }
    }

    /**
     * Fetches a single user by their email.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.let { userEntity ->
            mapEntityToDomain(userEntity)
        }
    }

    /**
     * Deletes a user.
     */
    suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            val userEntity = UserEntity(
                id = user.id,
                email = user.email,
                username = user.username,
                passwordHash = user.passwordHash,
                role = user.role,
                isTemp = user.isTemp,
                createdAt = user.createdAt
            )
            userDao.deleteUser(userEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to map from a database UserEntity to a domain User model.
     * This removes sensitive info like the password hash.
     */
    private fun mapEntityToDomain(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            email = userEntity.email,
            username = userEntity.username,
            passwordHash = "", // NEVER expose the real hash to the domain/UI layers
            role = userEntity.role,
            isTemp = userEntity.isTemp,
            createdAt = userEntity.createdAt
        )
    }
}
