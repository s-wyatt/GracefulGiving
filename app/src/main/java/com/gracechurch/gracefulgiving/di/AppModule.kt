package com.gracechurch.gracefulgiving.di

import com.gracechurch.gracefulgiving.data.repository.UserSessionRepositoryImpl
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCase
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindImportDonationsUseCase(
        impl: ImportDonationsUseCaseImpl
    ): ImportDonationsUseCase
}