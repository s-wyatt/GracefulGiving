package com.gracechurch.gracefulgiving.ui.usermanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.User
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val users = userRepository.getAllUsers()
            val currentUser = userSessionRepository.currentUser
            _uiState.update { it.copy(isLoading = false, users = users, currentUserRole = currentUser?.role) }
        }
    }

    fun inviteUser(invite: InviteUser) {
        viewModelScope.launch {
            userRepository.createUser(
                email = invite.email,
                username = invite.username,
                fullName = invite.fullName,
                password = "password", // This will be ignored, as a temporary password is being set
                role = invite.role,
                tempPassword = invite.tempPassword,
                avatarUri = invite.avatarUri
            )
            loadUsers() // Refresh the user list
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
            loadUsers() // Refresh the user list
        }
    }
}

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val currentUserRole: UserRole? = null
)

data class InviteUser(
    val email: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val tempPassword: String,
    val avatarUri: String?
)
