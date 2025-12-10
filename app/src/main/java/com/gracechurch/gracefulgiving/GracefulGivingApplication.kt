package com.gracechurch.gracefulgiving

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gracechurch.gracefulgiving.data.local.converters.Converters
import com.gracechurch.gracefulgiving.data.local.dao.BatchDao // Assuming you have this
import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity // Assuming you have this
import com.gracechurch.gracefulgiving.data.local.entity.CheckImageEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.entity.UserEntity

/**
 * The main database class for the application.
 *
 * This class is annotated with @Database and lists all the entities (tables)
 * and the database version. It also contains abstract methods for accessing each DAO.
 */
@Database(
    entities = [
        UserEntity::class,
        DonorEntity::class,
        BatchEntity::class, // Add this if you have a BatchEntity
        DonationEntity::class,
        CheckImageEntity::class
    ],
    version = 1,
    exportSchema = false // Set to false for development to avoid schema export warnings
)
@TypeConverters(Converters::class)

abstract class GracefulGivingDatabase : RoomDatabase() {

    // Abstract methods to provide access to each DAO.
    // Room's generated implementation will provide the concrete instances.
    abstract fun userDao(): UserDao
    abstract fun donorDao(): DonorDao
    abstract fun donationDao(): DonationDao
    abstract fun checkImageDao(): CheckImageDao
    abstract fun batchDao(): BatchDao // Add this if you have a BatchDao
}
