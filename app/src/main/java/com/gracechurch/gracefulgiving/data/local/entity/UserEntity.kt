package com.gracechurch.gracefulgiving.data.local.entity
import android.R
import androidx.room.*
import com.gracechurch.gracefulgiving.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    val tempPassword: String? = null,
    val isTemp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: Long? = null
)