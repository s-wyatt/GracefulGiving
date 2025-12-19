package com.gracechurch.gracefulgiving.domain.usecase

import android.util.Log
import com.gracechurch.gracefulgiving.data.local.dao.BatchDao
import com.gracechurch.gracefulgiving.data.local.dao.DonationDao
import com.gracechurch.gracefulgiving.data.local.dao.DonorDao
import com.gracechurch.gracefulgiving.data.local.dao.FundDao
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonorEntity
import com.gracechurch.gracefulgiving.data.local.entity.FundEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class FileProcessingService @Inject constructor(
    private val donorDao: DonorDao,
    private val fundDao: FundDao,
    private val batchDao: BatchDao,
    private val donationDao: DonationDao
) {

    suspend fun processFile(inputStream: InputStream, userId: Long, fileName: String = "Unknown File") {
        withContext(Dispatchers.IO) {
            Log.d("FileProcessingService", "Processing file: $fileName")

            val lines = inputStream.bufferedReader().readLines()

            if (lines.isEmpty()) {
                throw IllegalArgumentException("File is empty")
            }

            // Skip header row
            val dataLines = lines.drop(1)

            if (dataLines.isEmpty()) {
                throw IllegalArgumentException("No data rows found")
            }

            // Parse all CSV rows
            val csvRecords = dataLines.mapNotNull { line ->
                parseCsvLine(line)
            }

            if (csvRecords.isEmpty()) {
                throw IllegalArgumentException("No valid records found")
            }

            Log.d("FileProcessingService", "Parsed ${csvRecords.size} records")

            // Group by check date to create batches
            val recordsByDate: Map<Long, List<CsvRecord>> = csvRecords.groupBy { it.checkDate }

            // Process each date group
            recordsByDate.forEach { (checkDate, records) ->
                processBatchForDate(checkDate, records, userId)
            }

            Log.d("FileProcessingService", "Successfully imported ${csvRecords.size} donations")
        }
    }

    private suspend fun processBatchForDate(
        checkDate: Long,
        records: List<CsvRecord>,
        userId: Long
    ) {
        // Get the next batch number
        val nextBatchNumber: Long = batchDao.getNextBatchNumber()

        // Create batch for this date
        val batchEntity = BatchEntity(
            batchNumber = nextBatchNumber,
            userId = userId,
            createdOn = checkDate,
            status = "open",
            fundId = 1 // Default fund
        )
        val batchId: Long = batchDao.insertBatch(batchEntity)

        Log.d("FileProcessingService", "Created batch $nextBatchNumber for date ${formatDate(checkDate)}")

        // Process each donation in this batch
        records.forEach { record ->
            processDonation(record, batchId)
        }
    }

    private suspend fun processDonation(record: CsvRecord, batchId: Long) {
        // Get or create donor
        val donorId: Long = getOrCreateDonor(record.firstName, record.lastName)

        // Get or create fund
        val fundId: Long = getOrCreateFund(record.fundName)

        // Create donation
        val donation = DonationEntity(
            donorId = donorId,
            batchId = batchId,
            checkNumber = record.checkNumber,
            checkAmount = record.amount,
            checkDate = record.checkDate,
            fundId = fundId
        )

        donationDao.insertDonation(donation)

        Log.d("FileProcessingService",
            "Created donation: ${record.firstName} ${record.lastName} - $${record.amount}")
    }

    private suspend fun getOrCreateDonor(firstName: String, lastName: String): Long {
        // Check if donor already exists
        val existingDonor: DonorEntity? = donorDao.findDonorByName(firstName, lastName)

        return if (existingDonor != null) {
            existingDonor.donorId
        } else {
            // Create new donor
            val newDonor = DonorEntity(
                firstName = firstName,
                lastName = lastName
            )
            val insertedId: Long = donorDao.insertDonor(newDonor)
            insertedId
        }
    }

    private suspend fun getOrCreateFund(fundName: String): Long {
        // Check if fund already exists
        val existingFund: FundEntity? = fundDao.findFundByName(fundName)

        return if (existingFund != null) {
            existingFund.fundId
        } else {
            // Create new fund with placeholder values
            val newFund = FundEntity(
                name = fundName,
                bankName = "Unknown",
                accountName = "Unknown",
                accountNumber = "Unknown"
            )
            val insertedId: Long = fundDao.insertFund(newFund)
            insertedId
        }
    }

    private fun parseCsvLine(line: String): CsvRecord? {
        return try {
            val parts = line.split(",").map { it.trim() }

            if (parts.size < 6) {
                Log.w("FileProcessingService", "Skipping invalid line: $line")
                return null
            }

            // CSV format: "Last Name,First Name,Date,Amount,Account Name,Check #"
            val lastName = parts[0]
            val firstName = parts[1]
            val dateStr = parts[2]
            val amountStr = parts[3]
            val fundName = parts[4]
            val checkNumber = parts[5]

            // Parse date (adjust format to match your CSV)
            val checkDate = parseDate(dateStr)

            // Parse amount
            val amount = amountStr.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0

            CsvRecord(
                lastName = lastName,
                firstName = firstName,
                checkDate = checkDate,
                amount = amount,
                fundName = fundName,
                checkNumber = checkNumber
            )
        } catch (e: Exception) {
            Log.e("FileProcessingService", "Error parsing line: $line", e)
            null
        }
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            // Try common date formats - adjust these to match your CSV format
            val formats = listOf(
                "MM/dd/yyyy",
                "M/d/yyyy",
                "yyyy-MM-dd",
                "MM-dd-yyyy"
            )

            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.US)
                    sdf.isLenient = false
                    val date = sdf.parse(dateStr)
                    if (date != null) {
                        return date.time
                    }
                } catch (e: Exception) {
                    // Try next format
                }
            }

            Log.w("FileProcessingService", "Could not parse date: $dateStr, using current date")
            System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FileProcessingService", "Error parsing date: $dateStr", e)
            System.currentTimeMillis()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return sdf.format(timestamp)
    }

    private data class CsvRecord(
        val lastName: String,
        val firstName: String,
        val checkDate: Long,
        val amount: Double,
        val fundName: String,
        val checkNumber: String
    )
}