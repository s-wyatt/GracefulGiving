package com.gracechurch.gracefulgiving.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.gracechurch.gracefulgiving.domain.model.Donation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun printYearlyStatement(
    context: Context,
    donorName: String,
    donations: List<Donation>,
    year: String
): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    var y = 40f
    paint.textSize = 16f
    canvas.drawText("Yearly Giving Statement for $donorName - $year", 10f, y, paint)
    y += 40

    paint.textSize = 12f
    donations.forEach {
        canvas.drawText("Date: ${sdf.format(Date(it.date))}", 10f, y, paint)
        canvas.drawText("Check Number: ${it.checkNumber}", 200f, y, paint)
        canvas.drawText("Amount: $${it.amount}", 400f, y, paint)
        y += 20
    }

    y += 20
    canvas.drawLine(10f, y, 585f, y, paint)
    y += 20

    paint.textSize = 14f
    canvas.drawText("Total Donations: ${donations.size}", 10f, y, paint)
    y += 20
    canvas.drawText("Total Amount: $${"%.2f".format(donations.sumOf { it.amount })}", 10f, y, paint)

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "yearly_statement_${donorName.replace(" ", "_")}_$year.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "Statement saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving statement", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
    return file
}
