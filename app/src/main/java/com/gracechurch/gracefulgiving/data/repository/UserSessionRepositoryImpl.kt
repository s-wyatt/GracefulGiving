package com.gracechurch.gracefulgiving.data.repository

import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserSessionRepository that manages the current user session.
 * This is a singleton to ensure consistent state across the app.
 */
@Singleton
class UserSessionRepositoryImpl @Inject constructor() : UserSessionRepository {

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    override val currentUser: User?
        get() = _currentUserFlow.value

    /**
     * Sets the current user (typically called after login).
     */
    fun setCurrentUser(user: User) {
        _currentUserFlow.value = user
    }

    /**
     * Updates the current user in the session.
     * This should be called when the user's profile is edited.
     */
    override fun updateCurrentUser(user: User) {
        _currentUserFlow.value = user
    }

    /**
     * Logs out the current user.
     */
    override fun logout() {
        _currentUserFlow.value = null
    }
}