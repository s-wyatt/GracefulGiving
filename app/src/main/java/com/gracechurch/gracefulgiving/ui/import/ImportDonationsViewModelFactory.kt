package com.gracechurch.gracefulgiving.ui.import

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCase

class ImportDonationsViewModelFactory(
    private val importDonationsUseCase: ImportDonationsUseCase,
    private val userSessionRepository: UserSessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportDonationsViewModel::class.java)) {
            return ImportDonationsViewModel(importDonationsUseCase, userSessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}