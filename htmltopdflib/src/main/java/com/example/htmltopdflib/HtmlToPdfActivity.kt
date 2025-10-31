package com.example.htmltopdflib

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

/**
 * Activity that provides a ready-to-use UI for HTML to PDF conversion
 * Use this when you want to use the library's built-in UI
 */
class HtmlToPdfActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadButton: MaterialButton
    private lateinit var progressLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var converter: HtmlToPdfConverter

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FILE_NAME = "extra_file_name"
        const val EXTRA_RELATIVE_PATH = "extra_relative_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_to_pdf)

        // Initialize converter
        converter = HtmlToPdfConverter.create(this)

        // Initialize UI components
        initializeViews()
        setupClickListeners()

        // Check if URL was passed via intent
        intent.getStringExtra(EXTRA_URL)?.let { url ->
            urlInput.setText(url)
        }
    }

    private fun initializeViews() {
        urlInput = findViewById(R.id.urlInput)
        downloadButton = findViewById(R.id.downloadButton)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
    }

    private fun setupClickListeners() {
        downloadButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                startPdfGeneration(url)
            } else {
                showToast("Please enter a valid URL starting with http:// or https://")
            }
        }
    }

    private fun startPdfGeneration(url: String) {
        // Show progress
        showProgress(true)
        downloadButton.isEnabled = false

        // Get custom configuration from intent extras
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
        val relativePath = intent.getStringExtra(EXTRA_RELATIVE_PATH)

        // Build configuration
        val configBuilder = HtmlToPdfConfig.Builder(url)
        fileName?.let { configBuilder.fileName(it) }
        relativePath?.let { configBuilder.relativePath(it) }
        val config = configBuilder.build()

        // Convert with callback
        converter.convertAsync(config, object : HtmlToPdfCallback {
            override fun onSuccess(result: HtmlToPdfResult.Success) {
                showProgress(false)
                downloadButton.isEnabled = true
                showSuccessMessage(result.message)

                // Change button text and color temporarily
                downloadButton.text = "✓ PDF Created"
                downloadButton.backgroundTintList = ContextCompat.getColorStateList(
                    this@HtmlToPdfActivity,
                    R.color.success_color
                )

                // Reset button after 3 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    downloadButton.text = getString(R.string.convert_to_pdf)
                    downloadButton.backgroundTintList = ContextCompat.getColorStateList(
                        this@HtmlToPdfActivity,
                        R.color.primary_color
                    )
                }, 3000)
            }

            override fun onError(result: HtmlToPdfResult.Error) {
                showProgress(false)
                downloadButton.isEnabled = true
                showErrorMessage(result.message)
            }

            override fun onProgress(result: HtmlToPdfResult.Progress) {
                updateProgressText(result.message)
            }
        })
    }

    private fun showProgress(show: Boolean) {
        progressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateProgressText(text: String) {
        progressText.text = text
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
