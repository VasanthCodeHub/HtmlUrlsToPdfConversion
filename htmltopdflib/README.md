# HTML to PDF Library for Android

A powerful and easy-to-use Android library that converts HTML web pages to PDF documents. This library provides both a ready-to-use UI and a flexible API for custom integration.

## Features

- **High-Quality PDF Conversion**: Converts HTML web pages to PDF with preserved formatting
- **Two Integration Options**:
  - Pre-built UI Activity (quick integration)
  - Custom API for your own UI (full flexibility)
- **Asynchronous Processing**: Non-blocking conversion with progress callbacks
- **Scoped Storage Support**: Compatible with Android 10+ (API 29+) scoped storage
- **Customizable Configuration**: Filename, save path, user agent, timeout settings
- **Image Support**: Properly resolves and includes images from web pages
- **Modern Architecture**: Built with Kotlin coroutines for smooth performance

## Requirements

- Android API Level 27 (Android 8.1) or higher
- Kotlin support
- Internet permission for downloading web content

## Installation

### Step 1: Add the library to your project

Add the library module to your project:

```gradle
// In settings.gradle.kts
include(":htmltopdflib")
```

### Step 2: Add dependency

Add the dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":htmltopdflib"))
}
```

### Step 3: Add permissions

Add the following permissions to your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Usage

### Option 1: Using the Pre-built UI (Easiest)

The library includes a ready-to-use Activity with a beautiful Material Design UI.

```kotlin
import android.content.Intent
import com.example.htmltopdflib.HtmlToPdfActivity

class YourActivity : AppCompatActivity() {

    private fun launchPdfConverter() {
        val intent = Intent(this, HtmlToPdfActivity::class.java)

        // Optional: Pre-fill the URL
        intent.putExtra(HtmlToPdfActivity.EXTRA_URL, "https://example.com")

        // Optional: Customize the filename
        intent.putExtra(HtmlToPdfActivity.EXTRA_FILE_NAME, "my_document.pdf")

        // Optional: Customize the save path
        intent.putExtra(HtmlToPdfActivity.EXTRA_RELATIVE_PATH, "Download/MyApp/PDFs")

        startActivity(intent)
    }
}
```

### Option 2: Using Custom UI with the Converter API

For full control over your UI, use the `HtmlToPdfConverter` API directly.

#### Basic Usage

```kotlin
import com.example.htmltopdflib.HtmlToPdfConverter
import com.example.htmltopdflib.HtmlToPdfConfig
import com.example.htmltopdflib.HtmlToPdfCallback
import com.example.htmltopdflib.HtmlToPdfResult

class YourActivity : AppCompatActivity() {

    private lateinit var converter: HtmlToPdfConverter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize converter
        converter = HtmlToPdfConverter.create(this)
    }

    private fun convertToPdf(url: String) {
        // Create configuration
        val config = HtmlToPdfConfig(url)

        // Convert asynchronously with callback
        converter.convertAsync(config, object : HtmlToPdfCallback {
            override fun onSuccess(result: HtmlToPdfResult.Success) {
                Toast.makeText(this@YourActivity,
                    "PDF saved: ${result.pdfUri}",
                    Toast.LENGTH_LONG).show()
            }

            override fun onError(result: HtmlToPdfResult.Error) {
                Toast.makeText(this@YourActivity,
                    "Error: ${result.message}",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onProgress(result: HtmlToPdfResult.Progress) {
                // Update your progress UI
                updateProgress(result.message)
            }
        })
    }
}
```

#### Advanced Configuration

```kotlin
val config = HtmlToPdfConfig.Builder(url)
    .fileName("my_custom_file.pdf")
    .relativePath("Download/MyApp/PDFs")
    .userAgent("Mozilla/5.0 (Custom User Agent)")
    .timeout(60000) // 60 seconds
    .build()

converter.convertAsync(config, callback)
```

#### Synchronous Conversion (Background Thread)

If you need synchronous conversion (must be called from a background thread):

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

CoroutineScope(Dispatchers.IO).launch {
    val result = converter.convert(config)

    when (result) {
        is HtmlToPdfResult.Success -> {
            // Handle success
        }
        is HtmlToPdfResult.Error -> {
            // Handle error
        }
        is HtmlToPdfResult.Progress -> {
            // Won't be called in synchronous mode
        }
    }
}
```

## Configuration Options

### HtmlToPdfConfig

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `url` | String | Required | The URL of the webpage to convert |
| `fileName` | String | Auto-generated | Name of the PDF file to create |
| `relativePath` | String | "Download/Azzetta/asset/Insurance" | Relative path within Downloads directory |
| `userAgent` | String | Mozilla/5.0... | User agent string for HTTP requests |
| `timeout` | Int | 30000 | Connection timeout in milliseconds |

### Default File Naming

By default, files are named using the pattern: `BGR_Insurance_Policy_DD_MM_YYYY.pdf`

You can customize this using the `fileName` parameter.

## API Reference

### HtmlToPdfConverter

#### Methods

- `fun convertAsync(config: HtmlToPdfConfig, callback: HtmlToPdfCallback)` - Asynchronous conversion with callback
- `suspend fun convert(config: HtmlToPdfConfig, callback: HtmlToPdfCallback? = null): HtmlToPdfResult` - Synchronous conversion (background thread only)
- `companion fun create(context: Context): HtmlToPdfConverter` - Factory method to create converter instance

### HtmlToPdfCallback

Interface for receiving conversion callbacks:

```kotlin
interface HtmlToPdfCallback {
    fun onSuccess(result: HtmlToPdfResult.Success)
    fun onError(result: HtmlToPdfResult.Error)
    fun onProgress(result: HtmlToPdfResult.Progress)
}
```

### HtmlToPdfResult

Sealed class representing conversion results:

- `HtmlToPdfResult.Success` - Contains the PDF URI and success message
- `HtmlToPdfResult.Error` - Contains the exception and error message
- `HtmlToPdfResult.Progress` - Contains progress update message

## Architecture

The library uses:

- **iText 7** (7.2.3) - PDF generation engine
- **html2pdf** (4.0.3) - HTML to PDF conversion
- **JSoup** (1.15.3) - HTML parsing and fetching
- **Kotlin Coroutines** - Asynchronous processing
- **Material Components** - Modern UI design

## Storage Behavior

- **Android 10+ (API 29+)**: Uses MediaStore API for scoped storage
- **Android 9 and below**: Uses legacy external storage
- PDFs are saved to the Downloads directory by default
- Files can be accessed through the returned URI

## Error Handling

The library handles various error scenarios:

- Invalid URLs
- Network connectivity issues
- File creation failures
- HTML parsing errors
- PDF conversion errors

All errors are returned through the `HtmlToPdfCallback.onError()` method with descriptive messages.

## ProGuard Rules

The library includes consumer ProGuard rules automatically. If you encounter issues, ensure these rules are applied:

```proguard
-keep class com.itextpdf.** { *; }
-keep class org.jsoup.** { *; }
-keep class com.example.htmltopdflib.** { *; }
```

## Sample App

The project includes a sample app demonstrating both integration methods. See the `app` module for complete examples.

## Limitations

- Requires active internet connection to fetch web content
- Some complex JavaScript-heavy sites may not render perfectly
- Large pages may take longer to convert
- Minimum API level is 27 (Android 8.1)

## Dependencies

```gradle
implementation("com.itextpdf:itext7-core:7.2.3")
implementation("com.itextpdf:html2pdf:4.0.3")
implementation("org.jsoup:jsoup:1.15.3")
```

## License

This library is provided as-is for use in Android applications.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Support

For issues, questions, or feature requests, please open an issue on the project repository.

---

**Made with ❤️ for Android developers**
