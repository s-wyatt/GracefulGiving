package com.gracechurch.gracefulgiving.domain.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ExportDonationsUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val donationDao: DonationDao
) : ExportDonationsUseCase {

    override suspend fun execute(): String = withContext(Dispatchers.IO) {
        // Get all donations with donor and fund info
        val donations = donationDao.getAllDonationsWithDetails()

        if (donations.isEmpty()) {
            throw IllegalStateException("No donations to export")
        }

        // Get Documents folder
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        // Find next available export number
        val exportNumber = getNextExportNumber(documentsDir)

        // Create file
        val fileName = "Export $exportNumber.csv"
        val file = File(documentsDir, fileName)

        // Write CSV
        file.bufferedWriter().use { writer ->
            // Write header
            writer.write("First Name,Last Name,Check Date,Check Amount,Check Number,Fund Name")
            writer.newLine()

            // Write data rows
            donations.forEach { donation ->
                val row = buildCsvRow(
                    firstName = donation.donorFirstName,
                    lastName = donation.donorLastName,
                    checkDate = donation.checkDate,
                    checkAmount = donation.checkAmount,
                    checkNumber = donation.checkNumber,
                    fundName = donation.fundName
                )
                writer.write(row)
                writer.newLine()
            }
        }

        Log.d("ExportDonations", "Exported ${donations.size} donations to ${file.absolutePath}")

        return@withContext file.absolutePath
    }

    private fun getNextExportNumber(documentsDir: File): Int {
        val exportFiles = documentsDir.listFiles { _, name ->
            name.startsWith("Export ") && name.endsWith(".csv")
        } ?: emptyArray()

        val existingNumbers = exportFiles.mapNotNull { file ->
            val numberStr = file.name
                .removePrefix("Export ")
                .removeSuffix(".csv")
                .trim()
            numberStr.toIntOrNull()
        }.toSet()

        // Find the lowest unused number starting from 1
        var nextNumber = 1
        while (existingNumbers.contains(nextNumber)) {
            nextNumber++
        }

        return nextNumber
    }

    private fun buildCsvRow(
        firstName: String,
        lastName: String,
        checkDate: Long,
        checkAmount: Double,
        checkNumber: String,
        fundName: String
    ): String {
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val formattedDate = dateFormatter.format(checkDate)
        val formattedAmount = String.format(Locale.US, "%.2f", checkAmount)

        return listOf(
            escapeCsvField(firstName),
            escapeCsvField(lastName),
            formattedDate,
            formattedAmount,
            escapeCsvField(checkNumber),
            escapeCsvField(fundName)
        ).joinToString(",")
    }

    private fun escapeCsvField(field: String): String {
        // If field contains comma, quote, or newline, wrap in quotes and escape quotes
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}