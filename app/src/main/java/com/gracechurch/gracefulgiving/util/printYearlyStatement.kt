package com.gracechurch.gracefulgiving.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.gracechurch.gracefulgiving.domain.model.Donation
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a PDF yearly giving statement for a donor
 * @param context Android context
 * @param donorName Full name of the donor
 * @param donations List of donations for the year
 * @return File object pointing to the generated PDF
 */
fun printYearlyStatement(context: Context, donorName: String, donations: List<Donation>): File {
    if (donations.isEmpty()) {
        throw IllegalArgumentException("Cannot generate statement with no donations")
    }

    // Create PDF document
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // Letter size: 8.5" x 11"
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Paint objects for different text styles
    val titlePaint = Paint().apply {
        textSize = 24f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val headerPaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val normalPaint = Paint().apply {
        textSize = 12f
        isAntiAlias = true
    }

    val smallPaint = Paint().apply {
        textSize = 10f
        isAntiAlias = true
    }

    // Currency and date formatters
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    // Starting positions
    var yPosition = 80f
    val leftMargin = 50f
    val rightMargin = 562f

    // Church header
    canvas.drawText("Grace Church", leftMargin, yPosition, titlePaint)
    yPosition += 30f
    canvas.drawText("Yearly Giving Statement", leftMargin, yPosition, headerPaint)
    yPosition += 40f

    // Donor information
    canvas.drawText("Donor: $donorName", leftMargin, yPosition, normalPaint)
    yPosition += 20f

    // Extract year from donations
    val firstDonation = donations.first()
    val year = dateFormat.format(Date(firstDonation.checkDate)).split("/").last()
    canvas.drawText("Year: $year", leftMargin, yPosition, normalPaint)
    yPosition += 20f

    canvas.drawText("Statement Date: ${dateFormat.format(Date())}", leftMargin, yPosition, normalPaint)
    yPosition += 40f

    // Table header
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 20f

    canvas.drawText("Date", leftMargin, yPosition, headerPaint)
    canvas.drawText("Check #", leftMargin + 120f, yPosition, headerPaint)
    canvas.drawText("Amount", rightMargin - 100f, yPosition, headerPaint)
    yPosition += 5f

    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 20f

    // Donation rows
    var totalAmount = 0.0
    for (donation in donations.sortedBy { it.checkDate }) {
        // Check if we need a new page
        if (yPosition > 720f) {
            pdfDocument.finishPage(page)
            val newPage = pdfDocument.startPage(pageInfo)
            canvas.drawText("(continued)", leftMargin, 80f, normalPaint)
            yPosition = 100f
        }

        val donationDate = dateFormat.format(Date(donation.checkDate))
        val checkNum = donation.checkNumber ?: "N/A"
        val amount = currencyFormat.format(donation.checkAmount)

        canvas.drawText(donationDate, leftMargin, yPosition, normalPaint)
        canvas.drawText(checkNum, leftMargin + 120f, yPosition, normalPaint)
        canvas.drawText(amount, rightMargin - 100f, yPosition, normalPaint)

        totalAmount += donation.checkAmount
        yPosition += 20f
    }

    // Total section
    yPosition += 10f
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 25f

    canvas.drawText("Total Contributions:", leftMargin, yPosition, headerPaint)
    canvas.drawText(currencyFormat.format(totalAmount), rightMargin - 100f, yPosition, headerPaint)
    yPosition += 40f

    // Footer note
    canvas.drawText(
        "This statement is provided for your tax records. Grace Church is a 501(c)(3) organization.",
        leftMargin,
        yPosition,
        smallPaint
    )
    yPosition += 15f
    canvas.drawText(
        "No goods or services were provided in exchange for these contributions.",
        leftMargin,
        yPosition,
        smallPaint
    )

    // Finish the page
    pdfDocument.finishPage(page)

    // Save to file
    val fileName = "statement_${donorName.replace(" ", "_")}_$year.pdf"
    val file = File(context.cacheDir, fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
    } finally {
        pdfDocument.close()
    }

    return file
}