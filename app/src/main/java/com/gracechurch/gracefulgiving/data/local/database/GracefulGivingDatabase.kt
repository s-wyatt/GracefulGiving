package com.gracechurch.gracefulgiving.data.local.database
import com.gracechurch.gracefulgiving.data.local.dao.*
import com.gracechurch.gracefulgiving.data.local.entity.*
import androidx.room.*

@Database(
    entities = [UserEntity::class, DonorEntity::class, BatchEntity::class, DonationEntity::class, CheckImageEntity::class],
    version = 1
)
abstract class GracefulGivingDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun donorDao(): DonorDao
    abstract fun batchDao(): BatchDao
    abstract fun donationDao(): DonationDao
    abstract fun checkImageDao(): CheckImageDao
}