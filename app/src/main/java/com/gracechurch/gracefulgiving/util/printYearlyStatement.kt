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
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Paint objects for different text styles
    val titlePaint = Paint().apply {
        textSize = 20f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val subTitlePaint = Paint().apply {
        textSize = 16f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val headerPaint = Paint().apply {
        textSize = 12f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val normalPaint = Paint().apply {
        textSize = 12f
        isAntiAlias = true
    }

    // Currency and date formatters
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    // Starting positions
    var yPosition = 60f
    val leftMargin = 40f
    val rightMargin = 555f
    val center = (leftMargin + rightMargin) / 2

    // Church and statement header
    canvas.drawText("Grace Church", center, yPosition, titlePaint)
    yPosition += 25f
    canvas.drawText("Yearly Giving Statement for $donorName", center, yPosition, subTitlePaint)
    yPosition += 40f

    // Extract year from donations
    val firstDonation = donations.first()
    val year = dateFormat.format(Date(firstDonation.checkDate)).split("/").last()
    canvas.drawText("Tax Year: $year", leftMargin, yPosition, normalPaint)

    val statementDateText = "Statement Date: ${dateFormat.format(Date())}"
    val statementDateWidth = normalPaint.measureText(statementDateText)
    canvas.drawText(statementDateText, rightMargin - statementDateWidth, yPosition, normalPaint)
    yPosition += 40f

    // Table header
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 15f
    canvas.drawText("Date", leftMargin, yPosition, headerPaint)
    canvas.drawText("Check #", leftMargin + 150f, yPosition, headerPaint)
    headerPaint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Amount", rightMargin, yPosition, headerPaint)
    headerPaint.textAlign = Paint.Align.LEFT // Reset alignment
    yPosition += 5f
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 20f

    // Donation rows
    for (donation in donations.sortedBy { it.checkDate }) {
        val donationDate = dateFormat.format(Date(donation.checkDate))
        val amount = currencyFormat.format(donation.checkAmount)

        canvas.drawText(donationDate, leftMargin, yPosition, normalPaint)
        canvas.drawText(donation.checkNumber, leftMargin + 150f, yPosition, normalPaint)
        normalPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(amount, rightMargin, yPosition, normalPaint)
        normalPaint.textAlign = Paint.Align.LEFT // Reset alignment

        yPosition += 20f
    }

    // Total section line
    yPosition += 10f
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, normalPaint)
    yPosition += 25f

    // Totals
    val totalCountText = "Total Number of Donations: ${donations.size}"
    val totalAmount = donations.sumOf { it.checkAmount }
    val totalAmountText = "Total Contributions: ${currencyFormat.format(totalAmount)}"

    headerPaint.textAlign = Paint.Align.RIGHT
    canvas.drawText(totalCountText, rightMargin, yPosition, normalPaint)
    yPosition += 20f
    canvas.drawText(totalAmountText, rightMargin, yPosition, headerPaint)
    headerPaint.textAlign = Paint.Align.LEFT // Reset alignment
    yPosition += 50f

    // Footer note
    // ... (Your footer note logic is good and remains the same) ...

    // Finish the page
    pdfDocument.finishPage(page)

    // Save to file
    val fileName = "statement_${donorName.replace(" ", "_")}_$year.pdf"
    val file = File(context.cacheDir, fileName)

    try {
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
    } finally {
        pdfDocument.close()
    }

    return file
}
