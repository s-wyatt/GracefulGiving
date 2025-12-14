package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.util.PasswordUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun authenticateUser(
        email: String,
        password: String
    ): User? {
        val userEntity = userDao.getUserByEmail(email) ?: return null

        val passwordValid =
            if (userEntity.isTemp && userEntity.tempPassword != null) {
                password == userEntity.tempPassword
            } else {
                PasswordUtils.verifyPassword(password, userEntity.passwordHash)
            }

        return if (passwordValid) mapEntityToDomain(userEntity) else null
    }

    override suspend fun createUser(
        email: String,
        username: String,
        password: String,
        role: UserRole
    ): Result<Long> {
        return try {
            if (userDao.getUserByUsername(username) != null) {
                return Result.failure(Exception("Username already exists"))
            }
            if (userDao.getUserByEmail(email) != null) {
                return Result.failure(Exception("Email already exists"))
            }

            val entity = UserEntity(
                email = email,
                username = username,
                passwordHash = PasswordUtils.hashPassword(password),
                role = role,
                isTemp = false
            )

            Result.success(userDao.insertUser(entity))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(
        userId: Long,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))

            userDao.updateUser(
                user.copy(
                    passwordHash = PasswordUtils.hashPassword(newPassword),
                    isTemp = false,
                    tempPassword = null
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<User> =
        userDao.getAllUsers().map { mapEntityToDomain(it) }

    override suspend fun getUserById(userId: Long): User? =
        userDao.getUserById(userId)?.let { mapEntityToDomain(it) }

    override suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)?.let { mapEntityToDomain(it) }

    override suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            userDao.deleteUser(mapDomainToEntity(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapEntityToDomain(entity: UserEntity): User =
        User(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            role = entity.role,
            isTemp = entity.isTemp,
            createdAt = entity.createdAt
        )

    private fun mapDomainToEntity(user: User): UserEntity =
        UserEntity(
            id = user.id,
            email = user.email,
            username = user.username,
            passwordHash = "", // Not ideal, but we don't have the password hash here
            role = user.role,
            isTemp = user.isTemp,
            createdAt = user.createdAt
        )
}
