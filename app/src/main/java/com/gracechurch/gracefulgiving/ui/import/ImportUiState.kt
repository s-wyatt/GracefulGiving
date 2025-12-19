package com.gracechurch.gracefulgiving.ui.import

data class ImportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)