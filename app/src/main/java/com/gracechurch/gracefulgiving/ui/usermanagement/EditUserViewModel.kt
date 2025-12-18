package com.gracechurch.gracefulgiving.ui.usermanagement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                _uiState.update {
                    it.copy(
                        fullName = user.fullName,
                        email = user.email,
                        avatarUri = user.avatarUri
                    )
                }
            }
        }
    }

    fun getInitials(fullName: String): String {
        return fullName.split(' ')
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

    fun onFullNameChanged(fullName: String) {
        _uiState.update { it.copy(fullName = fullName, error = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onTempPasswordChanged(tempPassword: String) {
        _uiState.update { it.copy(tempPassword = tempPassword, error = null) }
    }

    fun onAvatarChanged(avatarUri: String) {
        if (avatarUri == "initials") {
            val initials = getInitials(_uiState.value.fullName)
            _uiState.update { it.copy(avatarUri = "initials:$initials", error = null) }
        } else {
            _uiState.update { it.copy(avatarUri = avatarUri, error = null) }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userRepository.getUserById(userId)
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }

            val updatedUser = user.copy(
                fullName = _uiState.value.fullName,
                email = _uiState.value.email,
                avatarUri = _uiState.value.avatarUri
            )

            userRepository.updateUser(updatedUser).onSuccess {
                if (_uiState.value.tempPassword.isNotBlank()) {
                    userRepository.setTemporaryPassword(userId, _uiState.value.tempPassword).onSuccess {
                        _uiState.update { it.copy(isLoading = false, isUserUpdated = true) }
                    }.onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to set temporary password"
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isUserUpdated = true) }
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "User update failed"
                    )
                }
            }
        }
    }
}

data class EditUserUiState(
    val fullName: String = "",
    val email: String = "",
    val tempPassword: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUserUpdated: Boolean = false
) {
    val isAvatarFile: Boolean
        get() = avatarUri != null && !avatarUri.startsWith("initials:") && !avatarUri.startsWith("icon:")
}
