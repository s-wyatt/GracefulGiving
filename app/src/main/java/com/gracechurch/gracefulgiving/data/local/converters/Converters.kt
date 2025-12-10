package com.gracechurch.gracefulgiving.data.local.converters

import androidx.room.TypeConverter
import com.gracechurch.gracefulgiving.domain.model.UserRole

/**
 * Type converters to allow Room to store complex types.
 */
class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value.uppercase())
    }
}
