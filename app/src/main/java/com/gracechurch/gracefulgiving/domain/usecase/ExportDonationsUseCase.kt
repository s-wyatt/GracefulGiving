package com.gracechurch.gracefulgiving.domain.usecase

interface ExportDonationsUseCase {
    suspend fun execute(): String // Returns the file path
}