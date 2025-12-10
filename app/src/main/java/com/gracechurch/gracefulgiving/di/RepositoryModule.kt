package com.gracechurch.gracefulgiving.di

import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.repository.CheckImageRepository
import com.gracechurch.gracefulgiving.data.repository.CheckImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCheckRepository(
        checkDao: CheckImageDao
    ): CheckImageRepository = CheckImageRepositoryImpl(checkDao)
}