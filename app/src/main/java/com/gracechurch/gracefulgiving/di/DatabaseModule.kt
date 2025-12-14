package com.gracechurch.gracefulgiving.di

import com.gracechurch.gracefulgiving.data.local.database.GracefulGivingDatabase

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gracechurch.gracefulgiving.data.local.dao.*
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // Initialize database with default admin user and funds
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        // Insert default admin user
                        val hashedPassword = hashPassword("admin")
                        val currentTime = System.currentTimeMillis()

                        db.execSQL("""
                            INSERT INTO users (id, email, username, passwordHash, role, tempPassword, isTemp, createdAt)
                            VALUES (1, 'admin@gbc.com', 'admin', '$hashedPassword', 'ADMIN', 'admin', 1, $currentTime)
                        """ )

                        // Insert default funds
                        db.execSQL("""
                            INSERT INTO funds (fundId, name, bankName, accountName, accountNumber)
                            VALUES (1, 'General Fund', 'Default Bank', 'General Account', '1234567890')
                        """ )
                        db.execSQL("""
                            INSERT INTO funds (fundId, name, bankName, accountName, accountNumber)
                            VALUES (2, 'Deacons'' Fund', 'Default Bank', 'Deacons Account', '0987654321')
                        """ )
                    }
                }
            })
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

    @Provides
    @Singleton
    fun provideFundDao(database: GracefulGivingDatabase): FundDao {
        return database.fundDao()
    }

    /**
     * Simple password hashing function
     * WARNING: This is a basic implementation for development only!
     * For production, use BCrypt, Argon2, or Android's built-in security libraries
     */
    private fun hashPassword(password: String): String {
        // Basic hash - REPLACE in production with proper hashing
        return password.hashCode().toString()
    }
}