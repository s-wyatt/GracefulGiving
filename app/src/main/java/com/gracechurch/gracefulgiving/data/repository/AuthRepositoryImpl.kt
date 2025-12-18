package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.repository.AuthRepository
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository // Use the interface
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val authenticatedUser = userRepository.authenticateUser(username, password)

            if (authenticatedUser != null) {
                userSessionRepository.updateCurrentUser(authenticatedUser) // Use the correct method
                Result.success(authenticatedUser)
            } else {
                // To diagnose the "Invalid password" error, let's get the user data
                val userForDebug = userRepository.getUserByUsername(username)
                if (userForDebug == null) {
                    Result.failure(Exception("User not found"))
                } else {
                    val debugMessage = "Invalid password. Debug info: isTemp=${userForDebug.isTemp}, tempPwd='${userForDebug.tempPassword}'"
                    Result.failure(Exception(debugMessage))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Clear the current user from the session
            userSessionRepository.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            if (!userRepository.verifyValidPassword(userId, currentPassword)) {
                Result.failure(Exception("Current password is incorrect"))
            } else {
                userRepository.updatePassword(userId, newPassword)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
