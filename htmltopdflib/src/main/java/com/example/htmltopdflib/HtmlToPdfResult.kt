package com.example.htmltopdflib

import android.net.Uri

/**
 * Result of HTML to PDF conversion operation
 */
sealed class HtmlToPdfResult {
    data class Success(val pdfUri: Uri, val message: String = "PDF created successfully") : HtmlToPdfResult()
    data class Error(val exception: Exception, val message: String = exception.message ?: "Unknown error") : HtmlToPdfResult()
    data class Progress(val message: String) : HtmlToPdfResult()
}
