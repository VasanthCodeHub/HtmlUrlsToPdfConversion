package com.example.htmlstopdf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.htmltopdflib.HtmlToPdfActivity
import com.example.htmltopdflib.HtmlToPdfCallback
import com.example.htmltopdflib.HtmlToPdfConfig
import com.example.htmltopdflib.HtmlToPdfConverter
import com.example.htmltopdflib.HtmlToPdfResult
import com.google.android.material.button.MaterialButton

/**
 * Demo Activity showing two ways to use the HTML to PDF library:
 * 1. Launch the library's built-in UI (HtmlToPdfActivity)
 * 2. Use custom UI with HtmlToPdfConverter API
 */
class MainActivity : AppCompatActivity() {

    private lateinit var launchLibraryUIButton: MaterialButton
    private lateinit var customUrlInput: EditText
    private lateinit var customConvertButton: MaterialButton
    private lateinit var customProgressLayout: LinearLayout
    private lateinit var customProgressText: TextView
    private lateinit var converter: HtmlToPdfConverter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize converter for custom UI option
        converter = HtmlToPdfConverter.create(this)

        // Initialize views
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        launchLibraryUIButton = findViewById(R.id.launchLibraryUIButton)
        customUrlInput = findViewById(R.id.customUrlInput)
        customConvertButton = findViewById(R.id.customConvertButton)
        customProgressLayout = findViewById(R.id.customProgressLayout)
        customProgressText = findViewById(R.id.customProgressText)
    }

    private fun setupClickListeners() {
        // Option 1: Launch library's built-in UI
        launchLibraryUIButton.setOnClickListener {
            launchLibraryUI()
        }

        // Option 2: Use custom UI with converter API
        customConvertButton.setOnClickListener {
            val url = customUrlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                convertWithCustomUI(url)
            } else {
                showToast("Please enter a URL")
            }
        }
    }

    /**
     * Option 1: Launch the library's built-in UI Activity
     * This is the simplest way to use the library - just launch the activity!
     */
    private fun launchLibraryUI() {
        val intent = Intent(this, HtmlToPdfActivity::class.java)

        // Optional: You can pass a URL to pre-fill the input
        // intent.putExtra(HtmlToPdfActivity.EXTRA_URL, "https://example.com")

        // Optional: You can customize the file name
        // intent.putExtra(HtmlToPdfActivity.EXTRA_FILE_NAME, "my_custom_file.pdf")

        // Optional: You can customize the save path
        // intent.putExtra(HtmlToPdfActivity.EXTRA_RELATIVE_PATH, "Download/MyApp/PDFs")

        startActivity(intent)
    }

    /**
     * Option 2: Use custom UI with the library's converter API
     * This gives you full control over the UI while using the library's conversion logic
     */
    private fun convertWithCustomUI(url: String) {
        // Show progress
        showCustomProgress(true)
        customConvertButton.isEnabled = false

        // Create configuration
        val config = HtmlToPdfConfig.Builder(url)
            .fileName("Custom_UI_PDF_${System.currentTimeMillis()}.pdf")
            .relativePath("Download/JackPdfConversion/pdf")
            .build()

        // Convert with callback
        converter.convertAsync(config, object : HtmlToPdfCallback {
            override fun onSuccess(result: HtmlToPdfResult.Success) {
                showCustomProgress(false)
                customConvertButton.isEnabled = true
                showToast("PDF saved successfully!")
                customUrlInput.text.clear()
            }

            override fun onError(result: HtmlToPdfResult.Error) {
                showCustomProgress(false)
                customConvertButton.isEnabled = true
                showToast("Error: ${result.message}")
            }

            override fun onProgress(result: HtmlToPdfResult.Progress) {
                updateCustomProgressText(result.message)
            }
        })
    }

    private fun showCustomProgress(show: Boolean) {
        customProgressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateCustomProgressText(text: String) {
        customProgressText.text = text
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
