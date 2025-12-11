package com.gracechurch.gracefulgiving.di

import android.content.Context
import androidx.room.Room
import com.gracechurch.gracefulgiving.data.local.database.GracefulGivingDatabase
import com.gracechurch.gracefulgiving.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GracefulGivingDatabase {
        return Room.databaseBuilder(
            context,
            GracefulGivingDatabase::class.java,
            "graceful_giving_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: GracefulGivingDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideDonorDao(database: GracefulGivingDatabase): DonorDao {
        return database.donorDao()
    }

    @Provides
    @Singleton
    fun provideBatchDao(database: GracefulGivingDatabase): BatchDao {
        return database.batchDao()
    }

    @Provides
    @Singleton
    fun provideDonationDao(database: GracefulGivingDatabase): DonationDao {
        return database.donationDao()
    }

    @Provides
    @Singleton
    fun provideCheckImageDao(database: GracefulGivingDatabase): CheckImageDao {
        return database.checkImageDao()
    }
}