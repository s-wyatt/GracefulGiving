package com.gracechurch.gracefulgiving.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gracechurch.gracefulgiving.domain.model.ScannedCheckData

/**
 * An ImageAnalyzer for CameraX that uses ML Kit to recognize text from a check.
 *
 * @param onScanComplete A callback lambda that is invoked when check data is successfully parsed.
 */
class CheckImageAnalyzer(
    private val onScanComplete: (ScannedCheckData) -> Unit
) : ImageAnalysis.Analyzer {

    // Get an instance of the ML Kit text recognizer
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    val textBlocks = visionText.textBlocks
                    if (textBlocks.isNotEmpty()) {
                        // Basic parsing logic (can be improved with more advanced regex)
                        val text = visionText.text
                        val name = parseName(text)
                        val amount = parseAmount(text)
                        val checkNumber = parseCheckNumber(text)

                        // If we found something that looks like an amount, consider it a success
                        if (amount.isNotBlank()) {
                            onScanComplete(
                                ScannedCheckData(
                                    firstName = name.first,
                                    lastName = name.second,
                                    amount = amount,
                                    checkNumber = checkNumber,
                                    imageData = null // imageData can be handled separately if needed
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // Log or handle the error appropriately
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    // It's crucial to close the imageProxy to allow the next frame to be processed.
                    imageProxy.close()
                }
        } else {
            imageProxy.close() // Close if the image is null
        }
    }

    // --- Parsing Helper Functions (These are very basic and can be improved) ---

    private fun parseName(text: String): Pair<String, String> {
        // A very simple heuristic: find the first two words that look like a name
        val lines = text.lines()
        for (line in lines) {
            val words = line.split(" ").filter { it.isNotBlank() && it.all { c -> c.isLetter() } }
            if (words.size >= 2) {
                return Pair(words[0], words[1])
            }
        }
        return Pair("", "")
    }

    private fun parseAmount(text: String): String {
        // Regex to find a dollar amount like $123.45 or 123.45
        val amountRegex = """(?:\$)?(\d{1,3}(?:,\d{3})*|\d+)(\.\d{2})""".toRegex()
        return amountRegex.find(text)?.groupValues?.get(0)?.replace("$", "") ?: ""
    }

    private fun parseCheckNumber(text: String): String {
        // A very simple heuristic: find a 3 to 5 digit number
        val checkNumberRegex = """\b(\d{3,5})\b""".toRegex()
        return checkNumberRegex.find(text)?.groupValues?.get(1) ?: ""
    }
}
