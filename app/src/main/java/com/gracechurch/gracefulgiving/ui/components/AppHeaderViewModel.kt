package com.gracechurch.gracefulgiving.ui.components

import androidx.lifecycle.ViewModel
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppHeaderViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {
    val currentUser = userSessionRepository.currentUserFlow
}