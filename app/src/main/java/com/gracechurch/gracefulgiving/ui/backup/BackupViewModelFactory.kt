package com.gracechurch.gracefulgiving.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gracechurch.gracefulgiving.domain.usecase.BackupDatabaseUseCase

class BackupViewModelFactory(
    private val backupDatabaseUseCase: BackupDatabaseUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            return BackupViewModel(backupDatabaseUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
