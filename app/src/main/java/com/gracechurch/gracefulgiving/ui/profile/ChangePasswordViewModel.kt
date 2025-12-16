package com.gracechurch.gracefulgiving.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onCurrentPasswordChanged(password: String) {
        _uiState.update { it.copy(currentPassword = password, error = null) }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password, error = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun changePassword() {
        viewModelScope.launch {
            if (_uiState.value.newPassword != _uiState.value.confirmPassword) {
                _uiState.update { it.copy(error = "Passwords do not match") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userSessionRepository.currentUser
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }

            userRepository.authenticateUser(user.email, _uiState.value.currentPassword)?.let {
                userRepository.updatePassword(user.id, _uiState.value.newPassword).onSuccess {
                    _uiState.update { it.copy(isLoading = false, isPasswordChanged = true) }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Password change failed"
                        )
                    }
                }
            } ?: _uiState.update { it.copy(isLoading = false, error = "Invalid current password") }
        }
    }
}

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordChanged: Boolean = false
)
