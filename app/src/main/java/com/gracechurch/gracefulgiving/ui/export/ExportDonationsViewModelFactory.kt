package com.gracechurch.gracefulgiving.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gracechurch.gracefulgiving.domain.usecase.ExportDonationsUseCase

class ExportDonationsViewModelFactory(
    private val exportDonationsUseCase: ExportDonationsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportDonationsViewModel::class.java)) {
            return ExportDonationsViewModel(exportDonationsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}