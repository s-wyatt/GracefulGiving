package com.gracechurch.gracefulgiving.util
import java.security.MessageDigest

object PasswordUtils {
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean = hashPassword(password) == hash
}