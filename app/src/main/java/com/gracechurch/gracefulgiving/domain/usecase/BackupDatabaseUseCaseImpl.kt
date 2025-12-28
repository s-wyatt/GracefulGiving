package com.gracechurch.gracefulgiving.domain.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.gracechurch.gracefulgiving.data.local.dao.AliasDao
import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.CheckImageDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.dao.FundDao
import com.gracechurch.gracefulgiving.data.local.dao.UserDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BackupDatabaseUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val donorDao: DonorDao,
    private val donationDao: DonationDao,
    private val batchDao: BatchDao,
    private val fundDao: FundDao,
    private val aliasDao: AliasDao,
    private val checkImageDao: CheckImageDao
) : BackupDatabaseUseCase {

    override suspend fun execute(): String = withContext(Dispatchers.IO) {
        val sql = StringBuilder()

        // Add header comment
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val backupDate = dateFormatter.format(Date())
        sql.appendLine("-- GracefulGiving Database Backup")
        sql.appendLine("-- Generated: $backupDate")
        sql.appendLine("-- SQLite compatible SQL dump")
        sql.appendLine()
        sql.appendLine("PRAGMA foreign_keys=OFF;")
        sql.appendLine("BEGIN TRANSACTION;")
        sql.appendLine()

        // Export funds table (must be before donations due to foreign key)
        sql.appendLine("-- Table: funds")
        sql.appendLine("DELETE FROM funds;")
        val funds = fundDao.getAllFunds()
        funds.forEach { fund ->
            sql.appendLine(
                "INSERT INTO funds (fundId, name, bankName, accountName, accountNumber) VALUES (" +
                "${fund.fundId}, " +
                "${escapeSql(fund.name)}, " +
                "${escapeSql(fund.bankName)}, " +
                "${escapeSql(fund.accountName)}, " +
                "${escapeSql(fund.accountNumber)});"
            )
        }
        sql.appendLine()

        // Export users table
        sql.appendLine("-- Table: users")
        sql.appendLine("DELETE FROM users;")
        val users = userDao.getAllUsers()
        users.forEach { user ->
            sql.appendLine(
                "INSERT INTO users (id, email, username, fullName, avatarUri, passwordHash, role, tempPassword, isTemp, createdAt, createdBy) VALUES (" +
                "${user.id}, " +
                "${escapeSql(user.email)}, " +
                "${escapeSql(user.username)}, " +
                "${escapeSql(user.fullName)}, " +
                "${escapeSqlNullable(user.avatarUri)}, " +
                "${escapeSql(user.passwordHash)}, " +
                "${escapeSql(user.role.name)}, " +
                "${escapeSqlNullable(user.tempPassword)}, " +
                "${if (user.isTemp) 1 else 0}, " +
                "${user.createdAt}, " +
                "${user.createdBy ?: "NULL"});"
            )
        }
        sql.appendLine()

        // Export donors table (must be before donations due to foreign key)
        sql.appendLine("-- Table: donors")
        sql.appendLine("DELETE FROM donors;")
        val donors = donorDao.getAllDonors()
        donors.forEach { donor ->
            sql.appendLine(
                "INSERT INTO donors (donorId, firstName, lastName) VALUES (" +
                "${donor.donorId}, " +
                "${escapeSql(donor.firstName)}, " +
                "${escapeSql(donor.lastName)});"
            )
        }
        sql.appendLine()

        // Export batches table (must be before donations due to foreign key)
        sql.appendLine("-- Table: batches")
        sql.appendLine("DELETE FROM batches;")
        val batches = batchDao.getAllBatchesList()
        batches.forEach { batch ->
            sql.appendLine(
                "INSERT INTO batches (batchId, batchNumber, userId, createdOn, status, fundId) VALUES (" +
                "${batch.batchId}, " +
                "${batch.batchNumber}, " +
                "${batch.userId}, " +
                "${batch.createdOn}, " +
                "${escapeSql(batch.status)}, " +
                "${batch.fundId});"
            )
        }
        sql.appendLine()

        // Export donations table
        sql.appendLine("-- Table: donations")
        sql.appendLine("DELETE FROM donations;")
        val donations = donationDao.getAllDonationsList()
        donations.forEach { donation ->
            sql.appendLine(
                "INSERT INTO donations (donationId, donorId, batchId, checkNumber, checkAmount, checkDate, checkImage, fundId) VALUES (" +
                "${donation.donationId}, " +
                "${donation.donorId}, " +
                "${donation.batchId}, " +
                "${escapeSql(donation.checkNumber)}, " +
                "${donation.checkAmount}, " +
                "${donation.checkDate}, " +
                "${escapeSqlNullable(donation.checkImage)}, " +
                "${donation.fundId});"
            )
        }
        sql.appendLine()

        // Export aliases table
        sql.appendLine("-- Table: aliases")
        sql.appendLine("DELETE FROM aliases;")
        val aliases = aliasDao.getAllAliases()
        aliases.forEach { alias ->
            sql.appendLine(
                "INSERT INTO aliases (aliasId, donorId, firstName, lastName) VALUES (" +
                "${alias.aliasId}, " +
                "${alias.donorId}, " +
                "${escapeSql(alias.firstName)}, " +
                "${escapeSql(alias.lastName)});"
            )
        }
        sql.appendLine()

        // Export check_images table
        sql.appendLine("-- Table: check_images")
        sql.appendLine("DELETE FROM check_images;")
        val checkImages = checkImageDao.getAllCheckImages()
        checkImages.forEach { checkImage ->
            sql.appendLine(
                "INSERT INTO check_images (checkImageId, donationId, batchId, donorId, imageData, capturedAt) VALUES (" +
                "${checkImage.checkImageId}, " +
                "${checkImage.donationId}, " +
                "${checkImage.batchId}, " +
                "${checkImage.donorId ?: "NULL"}, " +
                "${escapeSql(checkImage.imageData)}, " +
                "${checkImage.capturedAt});"
            )
        }
        sql.appendLine()

        sql.appendLine("COMMIT;")
        sql.appendLine("PRAGMA foreign_keys=ON;")

        // Get Documents folder
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        // Create file with timestamp
        val fileNameFormatter = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
        val timestamp = fileNameFormatter.format(Date())
        val fileName = "GracefulGiving_Backup_$timestamp.sql"
        val file = File(documentsDir, fileName)

        // Write SQL
        file.writeText(sql.toString())

        Log.d("BackupDatabase", "Database backed up to ${file.absolutePath}")

        return@withContext file.absolutePath
    }

    private fun escapeSql(value: String): String {
        val escaped = value.replace("'", "''")
        return "'$escaped'"
    }

    private fun escapeSqlNullable(value: String?): String {
        return if (value == null) "NULL" else escapeSql(value)
    }
}
