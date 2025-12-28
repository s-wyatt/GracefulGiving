package com.gracechurch.gracefulgiving.ui.backup

data class BackupUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val filePath: String? = null,
    val error: String? = null
)
