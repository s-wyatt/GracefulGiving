package com.gracechurch.gracefulgiving.domain.usecase

import java.io.InputStream

interface ImportDonationsUseCase {
    suspend fun execute(inputStream: InputStream, userId: Long)
}