package com.gracechurch.gracefulgiving.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.dao.FundDao
import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.entity.FundEntity
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DonorEntity::class,
        BatchEntity::class,
        DonationEntity::class,
        CheckImageEntity::class,
        FundEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GracefulGivingDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun donorDao(): DonorDao
    abstract fun batchDao(): BatchDao
    abstract fun donationDao(): DonationDao
    abstract fun checkImageDao(): CheckImageDao
    abstract fun fundDao(): FundDao
}