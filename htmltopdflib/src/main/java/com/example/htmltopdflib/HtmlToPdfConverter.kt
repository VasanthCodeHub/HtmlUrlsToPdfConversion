package com.example.htmltopdflib

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.OutputStream

/**
 * Main converter class for HTML to PDF operations
 * This is the primary API for custom UI integration
 */
class HtmlToPdfConverter(private val context: Context) {

    private val TAG = "HtmlToPdfConverter"

    /**
     * Convert HTML URL to PDF asynchronously with callback
     * @param config Configuration for the conversion
     * @param callback Callback to receive conversion results
     */
    fun convertAsync(config: HtmlToPdfConfig, callback: HtmlToPdfCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            convert(config, callback)
        }
    }

    /**
     * Convert HTML URL to PDF synchronously (must be called from background thread)
     * @param config Configuration for the conversion
     * @param callback Optional callback to receive progress updates
     * @return HtmlToPdfResult indicating success or failure
     */
    suspend fun convert(config: HtmlToPdfConfig, callback: HtmlToPdfCallback? = null): HtmlToPdfResult {
        return try {
            Log.d(TAG, "Starting conversion for URL: ${config.url}")

            // Validate URL
            if (!isValidUrl(config.url)) {
                val error = HtmlToPdfResult.Error(
                    IllegalArgumentException("Invalid URL"),
                    "Please provide a valid URL starting with http:// or https://"
                )
                withContext(Dispatchers.Main) { callback?.onError(error) }
                return error
            }

            // Step 1: Fetch HTML content
            notifyProgress("Downloading webpage content...", callback)
            val document = Jsoup.connect(config.url)
                .userAgent(config.userAgent)
                .timeout(config.timeout)
                .get()
            val htmlContent = document.html()
            Log.d(TAG, "HTML fetched successfully")

            // Step 2: Create PDF file
            notifyProgress("Creating PDF file...", callback)
            val pdfUri = createPdfFile(config.fileName, config.relativePath)
            if (pdfUri == null) {
                val error = HtmlToPdfResult.Error(
                    IllegalStateException("Failed to create PDF file"),
                    "Failed to create PDF file"
                )
                withContext(Dispatchers.Main) { callback?.onError(error) }
                return error
            }

            // Step 3: Open output stream
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(pdfUri)
            if (outputStream == null) {
                val error = HtmlToPdfResult.Error(
                    IllegalStateException("Failed to open output stream"),
                    "Failed to open output stream"
                )
                withContext(Dispatchers.Main) { callback?.onError(error) }
                return error
            }

            // Step 4: Convert HTML to PDF
            notifyProgress("Converting HTML to PDF...", callback)
            val converterProperties = ConverterProperties()
            converterProperties.setBaseUri(config.url) // Set base URI for image resolution
            HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties)
            outputStream.close()

            // Step 5: Mark download as complete (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                context.contentResolver.update(pdfUri, values, null, null)
            }

            Log.d(TAG, "PDF created successfully at $pdfUri")

            // Notify success
            val success = HtmlToPdfResult.Success(pdfUri, "PDF saved successfully!")
            withContext(Dispatchers.Main) { callback?.onSuccess(success) }
            success

        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF", e)
            val error = HtmlToPdfResult.Error(e, "Error: ${e.message}")
            withContext(Dispatchers.Main) { callback?.onError(error) }
            error
        }
    }

    /**
     * Validate URL format
     */
    private fun isValidUrl(url: String): Boolean {
        return url.isNotEmpty() && (url.startsWith("http://") || url.startsWith("https://"))
    }

    /**
     * Notify progress to callback
     */
    private suspend fun notifyProgress(message: String, callback: HtmlToPdfCallback?) {
        withContext(Dispatchers.Main) {
            callback?.onProgress(HtmlToPdfResult.Progress(message))
        }
    }

    /**
     * Creates a file in the Downloads directory using MediaStore (for Android 10+),
     * or using file system for older versions.
     */
    private fun createPdfFile(fileName: String, relativePath: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for scoped storage
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            context.contentResolver.insert(collection, contentValues)
        } else {
            // Use legacy file creation for Android 9 and below
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, relativePath.replace("Download/", ""))
            if (!targetDir.exists()) targetDir.mkdirs()

            val file = File(targetDir, fileName)
            file.createNewFile()
            Uri.fromFile(file)
        }
    }

    companion object {
        /**
         * Factory method to create HtmlToPdfConverter instance
         */
        fun create(context: Context): HtmlToPdfConverter {
            return HtmlToPdfConverter(context.applicationContext)
        }
    }
}
