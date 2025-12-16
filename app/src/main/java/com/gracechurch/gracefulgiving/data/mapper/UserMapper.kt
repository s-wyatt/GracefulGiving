package com.gracechurch.gracefulgiving.data.mapper

import com.gracechurch.gracefulgiving.data.local.entity.UserEntity
import com.gracechurch.gracefulgiving.domain.model.User

fun UserEntity.toUser(): User {
    return User(
        id = id,
        email = email,
        username = username,
        fullName = fullName,
        avatarUri = avatarUri,
        role = role,
        isTemp = isTemp,
        createdAt = createdAt
    )
}
