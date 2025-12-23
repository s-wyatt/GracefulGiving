package com.gracechurch.gracefulgiving.ui.export

data class ExportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val filePath: String? = null
)