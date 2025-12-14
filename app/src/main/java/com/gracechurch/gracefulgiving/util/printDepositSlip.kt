package com.gracechurch.gracefulgiving.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a PDF deposit slip for a given batch of donations.
 * @param context Android context for file operations.
 * @param bankSettings The bank information to display.
 * @param donations The list of donations included in the deposit.
 * @param batchDate The date of the batch.
 * @return A [File] object pointing to the generated PDF.
 */
fun printDepositSlip(
    context: Context,
    bankSettings: BankSettingsEntity?,
    donations: List<DonationWithDonor>,
    batchDate: Long
): File {
    if (bankSettings == null) {
        throw IllegalArgumentException("Bank settings are required to print a deposit slip.")
    }

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
    canvas.drawText("Bank Deposit Slip", center, yPosition, titlePaint)
    yPosition += 40f

    // 2. Bank and Account Info
    canvas.drawText("Bank Name:", leftMargin, yPosition, headerPaint)
    canvas.drawText(bankSettings.bankName, leftMargin + 150f, yPosition, normalPaint)
    yPosition += 20f
    canvas.drawText("Account Name:", leftMargin, yPosition, headerPaint)
    canvas.drawText(bankSettings.accountName, leftMargin + 150f, yPosition, normalPaint)
    yPosition += 20f
    canvas.drawText("Account Number:", leftMargin, yPosition, headerPaint)
    canvas.drawText(bankSettings.accountNumber, leftMargin + 150f, yPosition, normalPaint)
    yPosition += 20f
    canvas.drawText("Batch Date:", leftMargin, yPosition, headerPaint)
    canvas.drawText(dateFormat.format(Date(batchDate)), leftMargin + 150f, yPosition, normalPaint)
    yPosition += 40f

    // 3. Table Header for Checks
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
    yPosition += 15f
    canvas.drawText("Donor Name", leftMargin, yPosition, headerPaint)
    canvas.drawText("Check #", leftMargin + 250f, yPosition, headerPaint)
    headerPaint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Amount", rightMargin, yPosition, headerPaint)
    headerPaint.textAlign = Paint.Align.LEFT // Reset alignment
    yPosition += 5f
    canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, smallPaint)
    yPosition += 20f

    // 4. Donation (Check) Rows
    for (donation in donations.sortedBy { it.donor.lastName }) {
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

    // 5. Totals Section
    yPosition += 10f
    canvas.drawLine(rightMargin - 150f, yPosition, rightMargin, yPosition, normalPaint) // Line above totals
    yPosition += 25f

    val totalCountText = "Total Number of Checks: ${donations.size}"
    val totalAmount = donations.sumOf { it.donation.checkAmount }
    val totalAmountText = "Total Deposit Amount: ${currencyFormat.format(totalAmount)}"

    normalPaint.textAlign = Paint.Align.RIGHT
    headerPaint.textAlign = Paint.Align.RIGHT
    canvas.drawText(totalCountText, rightMargin, yPosition, normalPaint)
    yPosition += 25f
    canvas.drawText(totalAmountText, rightMargin, yPosition, headerPaint)

    // Finish the page
    pdfDocument.finishPage(page)

    // Save to file
    val fileName = "deposit_slip_${dateFormat.format(Date(batchDate)).replace('/', '-')}.pdf"
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
