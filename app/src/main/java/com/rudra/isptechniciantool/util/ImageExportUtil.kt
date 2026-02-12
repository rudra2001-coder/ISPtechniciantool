package com.rudra.isptechniciantool.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for exporting diagrams and maps to images.
 * Supports PNG/JPEG formats, quality settings, and sharing functionality.
 */
object ImageExportUtil {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Image format options for export.
     */
    enum class ImageFormat {
        PNG,
        JPEG
    }

    /**
     * Quality settings for image export.
     */
    data class ImageQuality(
        val format: ImageFormat = ImageFormat.PNG,
        val quality: Int = 100, // 0-100, only used for JPEG
        val scale: Float = 1.0f // Scale factor for higher resolution
    )

    /**
     * Result of image export operation.
     */
    sealed class ImageExportResult {
        data class Success(val uri: Uri, val filePath: String) : ImageExportResult()
        data class Error(val message: String) : ImageExportResult()
    }

    /**
     * Export options for customizing the image export process.
     */
    data class ExportOptions(
        val quality: ImageQuality = ImageQuality(),
        val fileName: String? = null,
        val backgroundColor: Color = Color.White,
        val includeTimestamp: Boolean = true
    )

    /**
     * Exports a Compose Canvas to a bitmap.
     */
    fun canvasToBitmap(
        width: Int,
        height: Int,
        drawCallback: (Canvas) -> Unit,
        quality: ImageQuality = ImageQuality()
    ): Bitmap {
        val scaledWidth = (width * quality.scale).toInt()
        val scaledHeight = (height * quality.scale).toInt()

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Scale canvas for higher resolution
        canvas.scale(quality.scale, quality.scale)

        drawCallback(canvas)

        return bitmap
    }

    /**
     * Captures a MapView as a bitmap.
     */
    fun captureMapView(mapView: MapView, quality: ImageQuality = ImageQuality()): Bitmap? {
        return try {
            // Ensure the map is fully rendered
            mapView.isDrawingCacheEnabled = true

            val originalBitmap = mapView.drawingCache
            if (originalBitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    (originalBitmap.width * quality.scale).toInt(),
                    (originalBitmap.height * quality.scale).toInt(),
                    true
                )
                originalBitmap.recycle()
                scaledBitmap
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Exports a bitmap to the gallery.
     */
    fun exportToGallery(
        context: Context,
        bitmap: Bitmap,
        options: ExportOptions = ExportOptions()
    ): ImageExportResult {
        return try {
            val fileName = options.fileName ?: generateImageFileName(options.quality.format)

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, getMimeType(options.quality.format))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ISPTool")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return ImageExportResult.Error("Failed to create media store entry")

            resolver.openOutputStream(uri)?.use { outputStream ->
                val compressFormat = getCompressFormat(options.quality.format)
                bitmap.compress(compressFormat, options.quality.quality, outputStream)
            } ?: run {
                resolver.delete(uri, null, null)
                return ImageExportResult.Error("Failed to open output stream")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            ImageExportResult.Success(uri, uri.toString())
        } catch (e: IOException) {
            ImageExportResult.Error("Failed to save image: ${e.message}")
        }
    }

    /**
     * Exports a bitmap to a file using SAF (Storage Access Framework).
     */
    fun exportToFile(
        context: Context,
        bitmap: Bitmap,
        options: ExportOptions = ExportOptions(),
        uri: Uri
    ): ImageExportResult {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val compressFormat = getCompressFormat(options.quality.format)
                bitmap.compress(compressFormat, options.quality.quality, outputStream)
            } ?: return ImageExportResult.Error("Failed to open output stream")

            ImageExportResult.Success(uri, uri.toString())
        } catch (e: IOException) {
            ImageExportResult.Error("Failed to save image: ${e.message}")
        }
    }

    /**
     * Exports a bitmap to app-specific storage.
     */
    fun exportToAppStorage(
        context: Context,
        bitmap: Bitmap,
        options: ExportOptions = ExportOptions()
    ): ImageExportResult {
        return try {
            val fileName = options.fileName ?: generateImageFileName(options.quality.format)
            val directory = File(context.filesDir, "exports")

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                val compressFormat = getCompressFormat(options.quality.format)
                bitmap.compress(compressFormat, options.quality.quality, outputStream)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ImageExportResult.Success(uri, file.absolutePath)
        } catch (e: IOException) {
            ImageExportResult.Error("Failed to save image: ${e.message}")
        }
    }

    /**
     * Creates a share intent for an image.
     */
    fun createShareIntent(context: Context, uri: Uri, format: ImageFormat): Intent {
        val mimeType = getMimeType(format)

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates a chooser intent for sharing an image.
     */
    fun createShareChooser(context: Context, uri: Uri, format: ImageFormat, title: String = "Share Image"): Intent {
        val shareIntent = createShareIntent(context, uri, format)
        return Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Gets the MIME type for an image format.
     */
    private fun getMimeType(format: ImageFormat): String {
        return when (format) {
            ImageFormat.PNG -> "image/png"
            ImageFormat.JPEG -> "image/jpeg"
        }
    }

    /**
     * Gets the compress format for a bitmap.
     */
    private fun getCompressFormat(format: ImageFormat): Bitmap.CompressFormat {
        return when (format) {
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        }
    }

    /**
     * Generates a default file name for image export.
     */
    fun generateImageFileName(format: ImageFormat = ImageFormat.PNG): String {
        val extension = when (format) {
            ImageFormat.PNG -> "png"
            ImageFormat.JPEG -> "jpg"
        }
        return "isp_diagram_${dateFormat.format(Date())}.$extension"
    }

    /**
     * Gets the file extension for an image format.
     */
    fun getFileExtension(format: ImageFormat): String {
        return when (format) {
            ImageFormat.PNG -> "png"
            ImageFormat.JPEG -> "jpg"
        }
    }
}
