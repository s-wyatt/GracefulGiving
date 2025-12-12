package com.gracechurch.gracefulgiving.di

import com.gracechurch.gracefulgiving.data.repository.BatchRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.DonationRepositoryImpl
import com.gracechurch.gracefulgiving.data.repository.DonorRepository
import com.gracechurch.gracefulgiving.data.repository.DonorRepositoryImpl
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBatchRepository(
        impl: BatchRepositoryImpl
    ): BatchRepository

    @Binds
    @Singleton
    abstract fun bindDonorRepository(
        impl: DonorRepositoryImpl
    ): DonorRepository
    @Binds
    abstract fun bindDonationRepository(
        donationRepositoryImpl: DonationRepositoryImpl
    ): DonationRepository
}
