package com.example.htmltopdflib

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Configuration class for HTML to PDF conversion
 */
data class HtmlToPdfConfig(
    val url: String,
    val fileName: String = generateDefaultFileName(),
    val relativePath: String = "Download/JackPdfConversion/pdf",
    val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    val timeout: Int = 30000
) {
    companion object {
        private fun generateDefaultFileName(): String {
            val formatter = SimpleDateFormat("d_M_yyyy", Locale.getDefault())
            val currentDate = formatter.format(Date())
            return "BGR_Insurance_Policy_$currentDate.pdf"
        }
    }

    /**
     * Builder pattern for creating HtmlToPdfConfig
     */
    class Builder(private val url: String) {
        private var fileName: String = generateDefaultFileName()
        private var relativePath: String = "Download/JackPdfConversion/pdf"
        private var userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private var timeout: Int = 30000

        fun fileName(fileName: String) = apply { this.fileName = fileName }
        fun relativePath(path: String) = apply { this.relativePath = path }
        fun userAgent(agent: String) = apply { this.userAgent = agent }
        fun timeout(timeout: Int) = apply { this.timeout = timeout }

        fun build() = HtmlToPdfConfig(
            url = url,
            fileName = fileName,
            relativePath = relativePath,
            userAgent = userAgent,
            timeout = timeout
        )
    }
}
