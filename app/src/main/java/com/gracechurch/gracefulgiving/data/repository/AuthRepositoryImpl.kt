package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.repository.AuthRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.util.PasswordUtils
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userSessionRepository: UserSessionRepository
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        // 1. Fetch user by username. If not found, login fails.
        val userEntity = userDao.getUserByUsername(username)
            ?: return Result.failure(Exception("Invalid username or password"))

        // 2. Determine if the password is valid based on the 'isTemp' flag.
        val passwordValid =
            if (userEntity.isTemp) {
                // 3a. If 'isTemp' is true, compare the input password directly
                // with the plain-text temporary password from the database.
                password == userEntity.tempPassword
            } else {
                // 3b. If 'isTemp' is false, use the secure verification method
                // to compare the input password with the hashed password.
                PasswordUtils.verifyPassword(password, userEntity.passwordHash)
            }

        // 4. If the password is valid, create the User session and return success.
        //    Otherwise, return failure.
        return if (passwordValid) {
            val user = User(
                id = userEntity.id,
                username = userEntity.username,
                email = userEntity.email,
                fullName = userEntity.fullName,
                avatarUri = userEntity.avatarUri,
                role = userEntity.role,
                isTemp = userEntity.isTemp,
                createdAt = userEntity.createdAt
            )
            userSessionRepository.currentUser = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }
}
