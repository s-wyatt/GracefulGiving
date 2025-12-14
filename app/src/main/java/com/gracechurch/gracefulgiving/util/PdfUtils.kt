package com.gracechurch.gracefulgiving.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.gracechurch.gracefulgiving.data.local.entity.BankSettingsEntity
import com.gracechurch.gracefulgiving.data.local.relations.DonationWithDonor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun printDepositSlip(
    context: Context,
    bankSettings: BankSettingsEntity?,
    donations: List<DonationWithDonor>,
    batchDate: Long
): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    var y = 40f
    paint.textSize = 12f

    canvas.drawText("Deposit Slip", 10f, y, paint)
    y += 20
    canvas.drawText("Batch Date: ${sdf.format(Date(batchDate))}", 10f, y, paint)
    y += 40

    bankSettings?.let {
        canvas.drawText("Bank Name: ${it.bankName}", 10f, y, paint)
        y += 20
        canvas.drawText("Account Name: ${it.accountName}", 10f, y, paint)
        y += 20
        canvas.drawText("Account Number: ${it.accountNumber}", 10f, y, paint)
        y += 40
    }

    canvas.drawText("Donations:", 10f, y, paint)
    y += 20

    donations.forEach {
        canvas.drawText("${it.donor.firstName} ${it.donor.lastName} - #${it.donation.checkNumber} - $${"%.2f".format(it.donation.checkAmount)}", 10f, y, paint)
        y += 20
    }

    y += 20
    canvas.drawText("Total Checks: ${donations.size}", 10f, y, paint)
    y += 20
    canvas.drawText("Total Amount: $${"%.2f".format(donations.sumOf { it.donation.checkAmount })}", 10f, y, paint)

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "deposit_slip.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "Deposit slip saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving deposit slip", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
    return file
}
