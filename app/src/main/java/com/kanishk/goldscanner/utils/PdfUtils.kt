package com.kanishk.goldscanner.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    
    /**
     * Save PDF data to a temporary file and open it with an external app
     */
    fun savePdfAndOpen(context: Context, pdfData: ByteArray, fileName: String = "invoice.pdf"): Boolean {
        return try {
            // Create temporary file in cache directory
            val file = File(context.cacheDir, fileName)
            
            // Write PDF data to file
            FileOutputStream(file).use { output ->
                output.write(pdfData)
                output.flush()
            }
            
            // Get content URI using FileProvider for API 24+
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Create intent to open PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Try to open with chooser, catch any exceptions
            try {
                context.startActivity(Intent.createChooser(intent, "Open PDF with"))
                true
            } catch (e: Exception) {
                // If chooser fails, try direct intent
                try {
                    context.startActivity(intent)
                    true
                } catch (e2: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}