package com.gracechurch.gracefulgiving.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gracechurch.gracefulgiving.BuildConfig
import com.gracechurch.gracefulgiving.data.local.dao.*
import com.gracechurch.gracefulgiving.data.local.database.GracefulGivingDatabase
import com.gracechurch.gracefulgiving.util.PasswordUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GracefulGivingDatabase {
        val databaseBuilder = Room.databaseBuilder(
            context,
            GracefulGivingDatabase::class.java,
            "graceful_giving.db"
        )

        if (!BuildConfig.DEBUG) {
            val passphrase = "his_grace_is_amazing".toByteArray()
            val factory = SupportFactory(passphrase)
            databaseBuilder.openHelperFactory(factory)
        }

        return databaseBuilder
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Use runBlocking to ensure this operation completes before any other
                    // database access can happen. This prevents a race condition on first login.
                    runBlocking {
                        val hashedPassword = PasswordUtils.hashPassword("admin")
                        val currentTime = System.currentTimeMillis()

                        db.execSQL("""
                            INSERT INTO users (id, email, username, fullName, passwordHash, role, tempPassword, isTemp, createdAt)
                            VALUES (1, 'admin@gbc.com', 'admin', 'Admin User', '$hashedPassword', 'ADMIN', 'admin', 1, $currentTime)
                        """)

                        db.execSQL("""
                            INSERT INTO funds (fundId, name, bankName, accountName, accountNumber)
                            VALUES (1, 'General Fund', 'Default Bank', 'General Account', '1234567890')
                        """)
                        db.execSQL("""
                            INSERT INTO funds (fundId, name, bankName, accountName, accountNumber)
                            VALUES (2, 'Deacons'' Fund', 'Default Bank', 'Deacons Account', '0987654321')
                        """)
                    }
                }
            })
            // CAUTION: destructive migration is enabled. Ensure autoMigrations are sufficient to avoid data loss.
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

    @Provides
    @Singleton
    fun provideAliasDao(database: GracefulGivingDatabase): AliasDao {
        return database.aliasDao()
    }
}
