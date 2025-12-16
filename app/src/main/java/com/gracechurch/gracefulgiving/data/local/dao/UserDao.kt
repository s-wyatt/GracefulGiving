package com.gracechurch.gracefulgiving.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY email")
    suspend fun getAllUsers(): List<UserEntity>
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :newPasswordHash, isTemp = 0, tempPassword = NULL WHERE id = :userId")
    suspend fun finalizePasswordChange(userId: Long, newPasswordHash: String)

    @Query("UPDATE users SET isTemp = 1, tempPassword = :tempPassword WHERE id = :userId")
    suspend fun setTemporaryPassword(userId: Long, tempPassword: String)

    @Query("UPDATE users SET fullName = :fullName, email = :email, avatarUri = :avatarUri WHERE id = :userId")
    suspend fun updateUserProfile(userId: Long, fullName: String, email: String, avatarUri: String?)
}
