package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        val userEntity = userDao.getUserByUsername(username)
        return if (userEntity != null && userEntity.passwordHash == password.hashCode().toString()) {
            Result.success(User(userEntity.id, userEntity.username, userEntity.role))
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }
}
