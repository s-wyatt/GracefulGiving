package com.gracechurch.gracefulgiving.di

import com.gracechurch.gracefulgiving.data.repository.AuthRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.BankSettingsRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.BatchRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.DonationRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.DonorRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.UserRepositoryImpl
import com.gracechurch.gracefulgiving.domain.repository.AuthRepository
import com.gracechurch.gracefulgiving.domain.repository.BankSettingsRepository
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.DonorRepository
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDonorRepository(
        donorRepositoryImpl: DonorRepositoryImpl
    ): DonorRepository

    @Binds
    abstract fun bindDonationRepository(
        donationRepositoryImpl: DonationRepositoryImpl
    ): DonationRepository

    @Binds
    abstract fun bindBankSettingsRepository(
        bankSettingsRepositoryImpl: BankSettingsRepositoryImpl
    ): BankSettingsRepository

    @Binds
    abstract fun bindBatchRepository(
        batchRepositoryImpl: BatchRepositoryImpl
    ): BatchRepository

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
