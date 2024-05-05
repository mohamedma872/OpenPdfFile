// Define the package of the application.
package com.example.myapplication

// Import necessary Android and Java classes.
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Main activity class, inheriting from AppCompatActivity.
class MainActivity : AppCompatActivity() {
    // Late-initialized properties for UI components and PDF rendering.
    private lateinit var imageView: ImageView // Image view to display PDF pages.
    private lateinit var pdfRenderer: PdfRenderer // Renderer for PDF content.
    private var currentPage: PdfRenderer.Page? = null // Optional current page of the PDF.
    private lateinit var nextButton: Button // Button to go to the next page.
    private lateinit var previousButton: Button // Button to go to the previous page.

    // Override the onCreate method to set up the activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view from the layout resource.

        // Initialize UI components from the layout.
        imageView = findViewById(R.id.imageView)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        // Post the PDF rendering until after the layout has been performed
        imageView.post {
            try {
                openRenderer(this)
                showPage(0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        // Set click listener for the next button.
        nextButton.setOnClickListener {
            currentPage?.let {
                val index = it.index + 1 // Calculate the next page number.
                if (index < (pdfRenderer.pageCount)) { // Check if the next page exists.
                    showPage(index) // Show the next page.
                }
            }
        }

        // Set click listener for the previous button.
        previousButton.setOnClickListener {
            currentPage?.let {
                val index = it.index - 1 // Calculate the previous page number.
                if (index >= 0) { // Check if the previous page exists.
                    showPage(index) // Show the previous page.
                }
            }
        }
    }

    // Method to open the PDF renderer.
    @Throws(IOException::class)
    private fun openRenderer(context: Context) {
        val fileDescriptor = getFileDescriptor(this, "dummmy.pdf") // Get the file descriptor for the PDF.
        if (fileDescriptor != null) {
            pdfRenderer = PdfRenderer(fileDescriptor) // Initialize the PdfRenderer.
        }
    }

    // Method to display a specific page of the PDF.
    private fun showPage(index: Int) {
        currentPage?.close() // Close the current page if it's open.
        currentPage = pdfRenderer.openPage(index).also { page ->
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888) // Create a bitmap for the page.
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) // Render the page into the bitmap.
            imageView.setImageBitmap(bitmap) // Set the bitmap to the image view.
        }
    }

    // Method to copy a file from assets to the cache directory.
    private fun copyFileToCache(context: Context, filename: String): File? {
        val assetManager = context.assets
        val outFile = File(context.cacheDir, filename)
        val inputStream = assetManager.open(filename)
        val outputStream = FileOutputStream(outFile)
        try {
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            return outFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }

    // Method to get a ParcelFileDescriptor for a file.
    private fun getFileDescriptor(context: Context, filename: String): ParcelFileDescriptor? {
        val file = copyFileToCache(context, filename) // Copy the file to cache.
        return if (file != null && file.exists()) {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY) // Open the file descriptor.
        } else {
            null
        }
    }

    // Override the onDestroy method to clean up resources.
    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close() // Close the current PDF page.
        pdfRenderer.close() // Close the PDF renderer.
    }
}
