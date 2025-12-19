package com.gracechurch.gracefulgiving.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    // Moved to RepositoryModule to consolidate binding
}