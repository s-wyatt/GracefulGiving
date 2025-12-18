package com.gracechurch.gracefulgiving.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.AuthRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScaffoldViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScaffoldUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionRepository.currentUserFlow.collect { user ->
                _uiState.update {
                    it.copy(
                        username = user?.username ?: "",
                        fullName = user?.fullName ?: "",
                        userRole = user?.role,
                        avatarUri = user?.avatarUri
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}

data class MainScaffoldUiState(
    val username: String = "",
    val fullName: String = "",
    val userRole: UserRole? = null,
    val isLoggedOut: Boolean = false,
    val avatarUri: String? = null
)
