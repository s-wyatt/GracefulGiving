package com.gracechurch.gracefulgiving.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity
import com.gracechurch.gracefulgiving.data.mapper.toUser
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Result

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : UserRepository {

    companion object {
        private val CURRENT_USER_ID_KEY = longPreferencesKey("current_user_id")
    }

    override suspend fun authenticateUser(email: String, password: String): User? {
        return try {
            val userEntity = userDao.getUserByEmail(email)
            if (userEntity != null && verifyPassword(password, userEntity.passwordHash)) {
                // Store current user ID after successful authentication
                setCurrentUserId(userEntity.id)
                userEntity.toUser()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createUser(
        email: String,
        username: String,
        fullName: String,
        password: String,
        role: UserRole,
        tempPassword: String?,
        avatarUri: String?
    ): Result<Long> {
        return try {
            val hashedPassword = hashPassword(password)
            val userEntity = UserEntity(
                email = email,
                username = username,
                fullName = fullName,
                passwordHash = hashedPassword,
                role = role,
                tempPassword = tempPassword,
                isTemp = tempPassword != null,
                avatarUri = avatarUri
            )
            val userId = userDao.insertUser(userEntity)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userEntity = userDao.getUserById(user.id)
                ?: return Result.failure(Exception("User not found"))

            val updatedEntity = userEntity.copy(
                email = user.email,
                username = user.username,
                fullName = user.fullName,
                avatarUri = user.avatarUri,
                role = user.role,
                isTemp = user.isTemp
            )
            userDao.updateUser(updatedEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(userId: Long, newPassword: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                val hashedPassword = hashPassword(newPassword)
                val updatedUser = user.copy(
                    passwordHash = hashedPassword,
                    isTemp = false,
                    tempPassword = null
                )
                userDao.updateUser(updatedUser)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTemporaryPassword(
        userId: Long,
        tempPassword: String
    ): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    tempPassword = tempPassword,
                    isTemp = true
                )
                userDao.updateUser(updatedUser)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map { it.toUser() }
    }

    override suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toUser()
    }

    override suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            val userEntity = userDao.getUserById(user.id)
            if (userEntity != null) {
                userDao.deleteUser(userEntity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW METHODS FOR CURRENT USER
    override suspend fun getCurrentUser(): User? {
        return try {
            val userId = context.dataStore.data
                .map { preferences -> preferences[CURRENT_USER_ID_KEY] }
                .first()

            if (userId != null) {
                userDao.getUserById(userId)?.toUser()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setCurrentUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID_KEY] = userId
        }
    }

    override suspend fun clearCurrentUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID_KEY)
        }
    }

    // Helper functions
    private fun hashPassword(password: String): String {
        // WARNING: This is a basic implementation for development only!
        // For production, use BCrypt, Argon2, or Android's built-in security libraries
        return password.hashCode().toString()
    }

    private fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        return hashPassword(inputPassword) == storedHash
    }
}
