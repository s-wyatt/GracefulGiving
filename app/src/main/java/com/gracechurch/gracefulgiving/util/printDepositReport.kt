package com.gracechurch.gracefulgiving.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import com.gracechurch.gracefulgiving.domain.model.Fund
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a PDF deposit report for a given batch of donations.
 * @param context Android context for file operations.
 * @param fund The fund information to display.
 * @param donations The list of donations included in the deposit.
 * @param batchDate The date of the batch.
 * @return A [File] object pointing to the generated PDF.
 */
fun printDepositReport(
    context: Context,
    fund: Fund?,
    donations: List<DonationWithDonor>,
    batchDate: Long
): File {
    if (fund == null) {
        throw IllegalArgumentException("Fund information is required to print a deposit report.")
    }

    // Separate checks and cash donations
    val checkDonations = donations.filter { it.donation.checkNumber.isNotEmpty() && it.donation.checkNumber != "Cash" }
    val cashDonations = donations.filter { it.donation.checkNumber.isEmpty() || it.donation.checkNumber == "Cash" }

    // Create PDF document
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // --- Paint objects for different text styles ---
    val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
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
    val smallPaint = Paint().apply {
        textSize = 10f
        isAntiAlias = true
        color = android.graphics.Color.GRAY
    }

    // --- Formatters ---
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    // --- Starting positions ---
    var yPosition = 60f
    val leftMargin = 40f
    val rightMargin = 555f
    val center = (leftMargin + rightMargin) / 2

    // --- PDF Content ---

    // 1. Header
    canvas.drawText("Deposit Report", center, yPosition, titlePaint)
    yPosition += 40f

    // 2. Bank and Account Info
    // Bank Name
    canvas.drawText("Bank:", leftMargin, yPosition, headerPaint)
    canvas.drawText(fund.bankName, leftMargin + 100f, yPosition, normalPaint)
    yPosition += 20f
    // Account Name
    canvas.drawText("Account:", leftMargin, yPosition, headerPaint)
    canvas.drawText(fund.accountName, leftMargin + 100f, yPosition, normalPaint)
    yPosition += 20f
    // Account Number
    canvas.drawText("Acc #:", leftMargin, yPosition, headerPaint)
    canvas.drawText(fund.accountNumber, leftMargin + 100f, yPosition, normalPaint)
    yPosition += 20f
    // Batch Date
    canvas.drawText("Date:", leftMargin, yPosition, headerPaint)
    canvas.drawText(dateFormat.format(Date(batchDate)), leftMargin + 100f, yPosition, normalPaint)
    yPosition += 40f

    // 3. CHECKS Section
    if (checkDonations.isNotEmpty()) {
        canvas.drawText("CHECKS", leftMargin, yPosition, headerPaint)
        yPosition += 10f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("Payee", leftMargin, yPosition, headerPaint)
        canvas.drawText("Check #", leftMargin + 250f, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Amount", rightMargin, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT // Reset alignment
        yPosition += 5f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 20f

        // Check Rows
        for (donation in checkDonations.sortedBy { it.donor.lastName }) {
            val donorName = "${donation.donor.firstName} ${donation.donor.lastName}"
            val amountText = currencyFormat.format(donation.donation.checkAmount)

            canvas.drawText(donorName, leftMargin, yPosition, normalPaint)
            canvas.drawText(donation.donation.checkNumber, leftMargin + 250f, yPosition, normalPaint)
            normalPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(amountText, rightMargin, yPosition, normalPaint)
            normalPaint.textAlign = Paint.Align.LEFT // Reset alignment

            yPosition += 20f
            // Add logic for new pages if yPosition exceeds page height
        }

        // Check Subtotal
        yPosition += 10f
        canvas.drawLine(rightMargin - 150f, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 20f
        val totalChecks = checkDonations.size
        val totalCheckAmount = checkDonations.sumOf { it.donation.checkAmount }
        normalPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total Checks: $totalChecks", rightMargin, yPosition, normalPaint)
        yPosition += 20f
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Check Total: ${currencyFormat.format(totalCheckAmount)}", rightMargin, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT
        normalPaint.textAlign = Paint.Align.LEFT
        yPosition += 40f
    }

    // 4. CASH Section
    if (cashDonations.isNotEmpty()) {
        canvas.drawText("CASH", leftMargin, yPosition, headerPaint)
        yPosition += 10f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("Payee", leftMargin, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Amount", rightMargin, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT // Reset alignment
        yPosition += 5f
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 20f

        // Cash Rows
        for (donation in cashDonations.sortedBy { it.donor.lastName }) {
            val donorName = "${donation.donor.firstName} ${donation.donor.lastName}"
            val amountText = currencyFormat.format(donation.donation.checkAmount)

            canvas.drawText(donorName, leftMargin, yPosition, normalPaint)
            normalPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(amountText, rightMargin, yPosition, normalPaint)
            normalPaint.textAlign = Paint.Align.LEFT // Reset alignment

            yPosition += 20f
            // Add logic for new pages if yPosition exceeds page height
        }

        // Cash Subtotal
        yPosition += 10f
        canvas.drawLine(rightMargin - 150f, yPosition, rightMargin, yPosition, smallPaint)
        yPosition += 20f
        val totalCash = cashDonations.size
        val totalCashAmount = cashDonations.sumOf { it.donation.checkAmount }
        normalPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total Cash Donations: $totalCash", rightMargin, yPosition, normalPaint)
        yPosition += 20f
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Cash Total: ${currencyFormat.format(totalCashAmount)}", rightMargin, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT
        normalPaint.textAlign = Paint.Align.LEFT
        yPosition += 40f
    }

    // 5. Grand Total Section
    yPosition += 10f
    canvas.drawLine(rightMargin - 200f, yPosition, rightMargin, yPosition, headerPaint)
    yPosition += 25f

    val totalDonations = donations.size
    val grandTotal = donations.sumOf { it.donation.checkAmount }

    normalPaint.textAlign = Paint.Align.RIGHT
    headerPaint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Total Donations: $totalDonations", rightMargin, yPosition, normalPaint)
    yPosition += 25f
    titlePaint.textAlign = Paint.Align.RIGHT
    canvas.drawText("GRAND TOTAL: ${currencyFormat.format(grandTotal)}", rightMargin, yPosition, titlePaint)

    // Finish the page
    pdfDocument.finishPage(page)

    // Save to file
    val fileName = "deposit_report_${dateFormat.format(Date(batchDate)).replace('/', '-')}.pdf"
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
