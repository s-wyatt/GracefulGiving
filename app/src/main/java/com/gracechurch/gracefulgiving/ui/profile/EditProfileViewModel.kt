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
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userSessionRepository.currentUser
            _uiState.update {
                it.copy(
                    fullName = user?.fullName ?: "",
                    email = user?.email ?: "",
                    avatarUri = user?.avatarUri
                )
            }
        }
    }

    fun onFullNameChanged(fullName: String) {
        _uiState.update { it.copy(fullName = fullName, error = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onAvatarChanged(avatarUri: String) {
        _uiState.update { it.copy(avatarUri = avatarUri, error = null) }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userSessionRepository.currentUser
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
                userSessionRepository.currentUser = updatedUser
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isProfileUpdated = true
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Profile update failed"
                    )
                }
            }
        }
    }
}

data class EditProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isProfileUpdated: Boolean = false
)
