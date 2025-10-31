package com.example.htmltopdflib

/**
 * Callback interface for HTML to PDF conversion operations
 */
interface HtmlToPdfCallback {
    /**
     * Called when conversion completes successfully
     * @param result The successful result containing PDF URI
     */
    fun onSuccess(result: HtmlToPdfResult.Success)

    /**
     * Called when conversion fails
     * @param result The error result containing exception and message
     */
    fun onError(result: HtmlToPdfResult.Error)

    /**
     * Called to report progress during conversion
     * @param result The progress result containing status message
     */
    fun onProgress(result: HtmlToPdfResult.Progress)
}
