package com.gracechurch.gracefulgiving.domain.repository

import com.gracechurch.gracefulgiving.domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for managing user session state.
 * This should be implemented to provide access to the currently logged-in user.
 */
interface UserSessionRepository {
    /**
     * The currently logged-in user, or null if no user is logged in.
     */
    val currentUser: User?

    /**
     * Flow of the current user for observing changes.
     */
    val currentUserFlow: StateFlow<User?>

    /**
     * Updates the current user in the session.
     * This should be called when the user's profile is edited.
     */
    fun updateCurrentUser(user: User)

    /**
     * Logs out the current user.
     */
    fun logout()
}