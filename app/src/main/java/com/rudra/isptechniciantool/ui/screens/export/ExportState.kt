package com.rudra.isptechniciantool.ui.screens.export

import android.net.Uri
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.util.DataExportUtil
import com.rudra.isptechniciantool.util.DataImportUtil
import com.rudra.isptechniciantool.util.ImageExportUtil
import com.rudra.isptechniciantool.util.TileDownloaderUtil

/**
 * UI state for the Export screen.
 */
data class ExportState(
    // Export state
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val exportedFilePath: String? = null,
    val exportedDevices: Int = 0,
    val exportedLinks: Int = 0,

    // Import state
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importSuccess: Boolean = false,
    val importError: String? = null,
    val importPreview: DataImportUtil.ImportPreview? = null,
    val importResult: DataImportUtil.ImportResult? = null,
    val selectedImportUri: Uri? = null,

    // Image export state
    val isImageExporting: Boolean = false,
    val imageExportProgress: Float = 0f,
    val imageExportSuccess: Boolean = false,
    val imageExportError: String? = null,
    val exportedImageUri: Uri? = null,

    // Tile download state
    val isTileDownloading: Boolean = false,
    val tileDownloadProgress: Float = 0f,
    val tileDownloadSuccess: Boolean = false,
    val tileDownloadError: String? = null,
    val tileEstimation: TileDownloaderUtil.StorageEstimation? = null,
    val downloadedTileCount: Int = 0,

    // Cache info
    val cacheSize: String = "0 B",
    val isLoadingCacheSize: Boolean = false,

    // Export options
    val exportOptions: ExportOptions = ExportOptions(),

    // Import options
    val importOptions: ImportOptions = ImportOptions(),

    // Tile download options
    val tileDownloadOptions: TileDownloadOptions = TileDownloadOptions(),

    // Data summary
    val totalDevices: Int = 0,
    val totalLinks: Int = 0,

    // Dialog states
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showImageExportDialog: Boolean = false,
    val showTileDownloadDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val showErrorMessage: Boolean = false
)

/**
 * Export options for data export.
 */
data class ExportOptions(
    val includeDevices: Boolean = true,
    val includeLinks: Boolean = true,
    val includeMonitoringData: Boolean = true,
    val includeNotes: Boolean = true,
    val fileName: String = DataExportUtil.generateFileName(),
    val description: String = ""
)

/**
 * Import options for data import.
 */
data class ImportOptions(
    val conflictResolution: DataImportUtil.ConflictResolution = DataImportUtil.ConflictResolution.SKIP,
    val validateData: Boolean = true,
    val preserveIds: Boolean = false,
    val remapIds: Boolean = true
)

/**
 * Tile download options.
 */
data class TileDownloadOptions(
    val boundingBox: TileDownloaderUtil.TileBoundingBox? = null,
    val minZoom: Int = 10,
    val maxZoom: Int = 17,
    val useCurrentMapView: Boolean = true
)

/**
 * Image export options.
 */
data class ImageExportOptions(
    val format: ImageExportUtil.ImageFormat = ImageExportUtil.ImageFormat.PNG,
    val quality: Int = 100,
    val scale: Float = 2.0f,
    val saveToGallery: Boolean = true,
    val shareAfterExport: Boolean = false
)

/**
 * Events that can occur in the Export screen.
 */
sealed class ExportEvent {
    // Export events
    object StartExport : ExportEvent()
    data class UpdateExportProgress(val progress: Float) : ExportEvent()
    object ExportComplete : ExportEvent()
    data class ExportError(val message: String) : ExportEvent()

    // Import events
    object StartImport : ExportEvent()
    data class UpdateImportProgress(val progress: Float) : ExportEvent()
    object ImportComplete : ExportEvent()
    data class ImportError(val message: String) : ExportEvent()
    data class SelectImportFile(val uri: Uri) : ExportEvent()
    data class UpdateImportOptions(val options: ImportOptions) : ExportEvent()

    // Image export events
    object StartImageExport : ExportEvent()
    data class UpdateImageExportProgress(val progress: Float) : ExportEvent()
    object ImageExportComplete : ExportEvent()
    data class ImageExportError(val message: String) : ExportEvent()

    // Tile download events
    object StartTileDownload : ExportEvent()
    data class UpdateTileDownloadProgress(val progress: Float) : ExportEvent()
    object TileDownloadComplete : ExportEvent()
    data class TileDownloadError(val message: String) : ExportEvent()

    // Cache events
    object LoadCacheSize : ExportEvent()
    data class UpdateCacheSize(val size: String) : ExportEvent()
    object ClearCache : ExportEvent()

    // Dialog events
    object ShowExportDialog : ExportEvent()
    object HideExportDialog : ExportEvent()
    object ShowImportDialog : ExportEvent()
    object HideImportDialog : ExportEvent()
    object ShowImageExportDialog : ExportEvent()
    object HideImageExportDialog : ExportEvent()
    object ShowTileDownloadDialog : ExportEvent()
    object HideTileDownloadDialog : ExportEvent()

    // Options events
    data class UpdateExportOptions(val options: ExportOptions) : ExportEvent()
    data class UpdateTileDownloadOptions(val options: TileDownloadOptions) : ExportEvent()

    // Misc events
    object DismissMessage : ExportEvent()
    object ShareExportedFile : ExportEvent()
    object ShareExportedImage : ExportEvent()
}

/**
 * UI messages for the Export screen.
 */
sealed class ExportMessage {
    data class Success(val message: String) : ExportMessage()
    data class Error(val message: String) : ExportMessage()
}
