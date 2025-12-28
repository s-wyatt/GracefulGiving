package com.gracechurch.gracefulgiving.domain.usecase

interface BackupDatabaseUseCase {
    suspend fun execute(): String // Returns the file path
}
