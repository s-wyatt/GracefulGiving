package com.gracechurch.gracefulgiving.domain.usecase

import java.io.InputStream
import javax.inject.Inject

class ImportDonationsUseCaseImpl @Inject constructor(
    private val fileProcessingService: FileProcessingService
) : ImportDonationsUseCase {

    override suspend fun execute(inputStream: InputStream, userId: Long) {
        fileProcessingService.processFile(inputStream, userId)
    }
}