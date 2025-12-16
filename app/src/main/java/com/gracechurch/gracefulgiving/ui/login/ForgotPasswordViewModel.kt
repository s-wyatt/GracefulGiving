package com.gracechurch.gracefulgiving.ui.login

import android.util.Log
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
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null, message = null) }
    }

    fun resetPassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            val user = userRepository.getUserByEmail(_uiState.value.email)
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }

            val tempPassword = generateTemporaryPassword()
            userRepository.setTemporaryPassword(user.id, tempPassword).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "A temporary password has been generated. Please check the logs.",
                        isPasswordReset = true
                    )
                }
                // In a real app, you would email this to the user.
                Log.d("ForgotPassword", "Temporary password for ${user.email}: $tempPassword")
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Password reset failed"
                    )
                }
            }
        }
    }

    private fun generateTemporaryPassword(): String {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isPasswordReset: Boolean = false
)
