package com.rudra.isptechniciantool.ui.screens.export

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import com.rudra.isptechniciantool.domain.repository.LinkRepository
import com.rudra.isptechniciantool.util.DataExportUtil
import com.rudra.isptechniciantool.util.DataImportUtil
import com.rudra.isptechniciantool.util.ImageExportUtil
import com.rudra.isptechniciantool.util.TileDownloaderUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the Export screen.
 * Handles data export/import, image export, and tile downloading.
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val linkRepository: LinkRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    init {
        loadDataCounts()
        loadCacheSize()
    }

    /**
     * Load the counts of devices and links.
     */
    private fun loadDataCounts() {
        viewModelScope.launch {
            deviceRepository.observeAllDevices().collect { devices ->
                _state.update { it.copy(totalDevices = devices.size) }
            }
        }
        viewModelScope.launch {
            linkRepository.observeAllLinks().collect { links ->
                _state.update { it.copy(totalLinks = links.size) }
            }
        }
    }

    /**
     * Load the current cache size.
     */
    private fun loadCacheSize() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCacheSize = true) }
            try {
                val size = TileDownloaderUtil.getCacheSize(context)
                _state.update {
                    it.copy(
                        cacheSize = TileDownloaderUtil.formatFileSize(size),
                        isLoadingCacheSize = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingCacheSize = false) }
            }
        }
    }

    /**
     * Export data to JSON file.
     */
    fun exportData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isExporting = true,
                    exportProgress = 0f,
                    exportSuccess = false,
                    exportError = null
                )
            }

            try {
                val devices = deviceRepository.getAllDevices()
                val links = linkRepository.getAllLinks()

                _state.update { it.copy(exportProgress = 0.3f) }

                val options = DataExportUtil.ExportOptions(
                    includeDevices = state.value.exportOptions.includeDevices,
                    includeLinks = state.value.exportOptions.includeLinks,
                    includeNotes = state.value.exportOptions.includeNotes,
                    fileName = state.value.exportOptions.fileName,
                    description = state.value.exportOptions.description
                )

                _state.update { it.copy(exportProgress = 0.6f) }

                val exportDir = File(context.filesDir, "exports")
                val result = DataExportUtil.exportToFile(
                    devices = devices,
                    links = links,
                    directory = exportDir,
                    options = options
                )

                _state.update { it.copy(exportProgress = 1f) }

                result.fold(
                    onSuccess = { filePath ->
                        _state.update {
                            it.copy(
                                isExporting = false,
                                exportSuccess = true,
                                exportedFilePath = filePath,
                                exportedDevices = devices.size,
                                exportedLinks = links.size,
                                showSuccessMessage = true
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isExporting = false,
                                exportError = error.message ?: "Export failed",
                                showErrorMessage = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        exportError = e.message ?: "Export failed",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Preview import data from a file.
     */
    fun previewImport(uri: Uri) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    selectedImportUri = uri,
                    isImporting = true
                )
            }

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")

                val json = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                val parseResult = DataImportUtil.parseJson(json)
                parseResult.fold(
                    onSuccess = { exportData ->
                        val importOptions = DataImportUtil.ImportOptions(
                            conflictResolution = when (state.value.importOptions.conflictResolution) {
                                DataImportUtil.ConflictResolution.OVERWRITE -> DataImportUtil.ConflictResolution.OVERWRITE
                                DataImportUtil.ConflictResolution.MERGE -> DataImportUtil.ConflictResolution.MERGE
                                else -> DataImportUtil.ConflictResolution.SKIP
                            },
                            validateData = state.value.importOptions.validateData,
                            preserveIds = state.value.importOptions.preserveIds,
                            remapIds = state.value.importOptions.remapIds
                        )
                        val preview = DataImportUtil.previewImport(exportData, importOptions)
                        _state.update {
                            it.copy(
                                isImporting = false,
                                importPreview = preview
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isImporting = false,
                                importError = error.message ?: "Invalid file format",
                                showErrorMessage = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        importError = e.message ?: "Failed to read file",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Import data from the selected file.
     */
    fun importData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isImporting = true,
                    importProgress = 0f,
                    importSuccess = false,
                    importError = null
                )
            }

            try {
                val uri = state.value.selectedImportUri
                    ?: throw Exception("No file selected")

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")

                val json = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                _state.update { it.copy(importProgress = 0.3f) }

                val exportDataResult = DataImportUtil.parseJson(json)
                exportDataResult.fold(
                    onSuccess = { exportData ->
                        _state.update { it.copy(importProgress = 0.5f) }

                        // Get existing IDs for conflict resolution
                        val existingDeviceIds = deviceRepository.getAllDevices().map { it.id }.toSet()
                        val existingLinkIds = linkRepository.getAllLinks().map { it.id }.toSet()

                        val options = DataImportUtil.ImportOptions(
                            conflictResolution = state.value.importOptions.conflictResolution,
                            validateData = state.value.importOptions.validateData,
                            preserveIds = state.value.importOptions.preserveIds,
                            remapIds = state.value.importOptions.remapIds
                        )

                        _state.update { it.copy(importProgress = 0.7f) }

                        val result = DataImportUtil.importData(
                            exportData = exportData,
                            existingDeviceIds = existingDeviceIds,
                            existingLinkIds = existingLinkIds,
                            options = options
                        )

                        // Save imported devices
                        result.importedDevices.forEach { device ->
                            if (device.id == 0L) {
                                deviceRepository.createDevice(device)
                            } else {
                                deviceRepository.updateDevice(device)
                            }
                        }

                        // Save imported links
                        result.importedLinks.forEach { link ->
                            if (link.id == 0L) {
                                linkRepository.createLink(link)
                            } else {
                                linkRepository.updateLink(link)
                            }
                        }

                        _state.update {
                            it.copy(
                                isImporting = false,
                                importProgress = 1f,
                                importSuccess = true,
                                importResult = result,
                                showSuccessMessage = true
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isImporting = false,
                                importError = error.message ?: "Import failed",
                                showErrorMessage = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        importError = e.message ?: "Import failed",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Export topology image.
     */
    fun exportImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isImageExporting = true,
                    imageExportProgress = 0f,
                    imageExportSuccess = false,
                    imageExportError = null
                )
            }

            try {
                val quality = ImageExportUtil.ImageQuality(
                    format = ImageExportUtil.ImageFormat.PNG,
                    quality = 100,
                    scale = 2f
                )

                val options = ImageExportUtil.ExportOptions(
                    quality = quality,
                    fileName = ImageExportUtil.generateImageFileName()
                )

                _state.update { it.copy(imageExportProgress = 0.5f) }

                val result = withContext(Dispatchers.IO) {
                    ImageExportUtil.exportToGallery(context, bitmap, options)
                }

                _state.update { it.copy(imageExportProgress = 1f) }

                when (result) {
                    is ImageExportUtil.ImageExportResult.Success -> {
                        _state.update {
                            it.copy(
                                isImageExporting = false,
                                imageExportSuccess = true,
                                exportedImageUri = result.uri,
                                showSuccessMessage = true
                            )
                        }
                    }
                    is ImageExportUtil.ImageExportResult.Error -> {
                        _state.update {
                            it.copy(
                                isImageExporting = false,
                                imageExportError = result.message,
                                showErrorMessage = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImageExporting = false,
                        imageExportError = e.message ?: "Export failed",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Download offline tiles.
     */
    fun downloadTiles(mapView: org.osmdroid.views.MapView) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isTileDownloading = true,
                    tileDownloadProgress = 0f,
                    tileDownloadSuccess = false,
                    tileDownloadError = null
                )
            }

            try {
                val boundingBox = if (state.value.tileDownloadOptions.useCurrentMapView) {
                    TileDownloaderUtil.TileBoundingBox.fromMapView(mapView)
                } else {
                    state.value.tileDownloadOptions.boundingBox
                }

                if (boundingBox == null) {
                    _state.update {
                        it.copy(
                            isTileDownloading = false,
                            tileDownloadError = "Please select a bounding box area",
                            showErrorMessage = true
                        )
                    }
                    return@launch
                }

                _state.update { it.copy(tileDownloadProgress = 0.2f) }

                // Estimate storage
                val estimation = TileDownloaderUtil.estimateStorage(
                    boundingBox = boundingBox!!,
                    options = TileDownloaderUtil.DownloadOptions(
                        minZoom = state.value.tileDownloadOptions.minZoom,
                        maxZoom = state.value.tileDownloadOptions.maxZoom
                    )
                )
                _state.update { it.copy(tileEstimation = estimation) }

                val options = TileDownloaderUtil.DownloadOptions(
                    minZoom = state.value.tileDownloadOptions.minZoom,
                    maxZoom = state.value.tileDownloadOptions.maxZoom
                )

                _state.update { it.copy(tileDownloadProgress = 0.4f) }

                val result = TileDownloaderUtil.downloadTiles(
                    context = context,
                    mapView = mapView,
                    boundingBox = boundingBox!!,
                    options = options,
                    callback = object : TileDownloaderUtil.DownloadProgressCallback {
                        override fun onProgressUpdate(current: Int, total: Int, percentage: Int) {
                            _state.update {
                                it.copy(
                                    tileDownloadProgress = 0.4f + (percentage / 100f) * 0.5f,
                                    downloadedTileCount = current
                                )
                            }
                        }

                        override fun onDownloadComplete(totalDownloaded: Int) {
                            _state.update {
                                it.copy(
                                    tileDownloadProgress = 1f,
                                    tileDownloadSuccess = true,
                                    downloadedTileCount = totalDownloaded,
                                    showSuccessMessage = true
                                )
                            }
                            loadCacheSize()
                        }

                        override fun onError(error: String) {
                            _state.update {
                                it.copy(
                                    tileDownloadError = error,
                                    showErrorMessage = true
                                )
                            }
                        }
                    }
                )

                result.fold(
                    onSuccess = { count ->
                        _state.update {
                            it.copy(
                                isTileDownloading = false,
                                tileDownloadProgress = 1f,
                                downloadedTileCount = count
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isTileDownloading = false,
                                tileDownloadError = error.message ?: "Download failed",
                                showErrorMessage = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isTileDownloading = false,
                        tileDownloadError = e.message ?: "Download failed",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Clear the tile cache.
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                TileDownloaderUtil.clearCache(context)
                loadCacheSize()
                _state.update {
                    it.copy(showSuccessMessage = true)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        tileDownloadError = e.message ?: "Failed to clear cache",
                        showErrorMessage = true
                    )
                }
            }
        }
    }

    /**
     * Estimate storage for current tile download options.
     */
    fun estimateStorage(boundingBox: TileDownloaderUtil.TileBoundingBox) {
        val options = TileDownloaderUtil.DownloadOptions(
            minZoom = state.value.tileDownloadOptions.minZoom,
            maxZoom = state.value.tileDownloadOptions.maxZoom
        )
        val estimation = TileDownloaderUtil.estimateStorage(boundingBox, options)
        _state.update { it.copy(tileEstimation = estimation) }
    }

    /**
     * Create share intent for exported file.
     */
    fun getShareIntent(): android.content.Intent? {
        val uri = state.value.exportedFilePath?.let { File(it) }?.let {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
        }
        return uri?.let { ImageExportUtil.createShareIntent(context, it, ImageExportUtil.ImageFormat.PNG) }
    }

    /**
     * Create share intent for exported image.
     */
    fun getImageShareIntent(): android.content.Intent? {
        val uri = state.value.exportedImageUri ?: return null
        return ImageExportUtil.createShareIntent(context, uri, ImageExportUtil.ImageFormat.PNG)
    }

    /**
     * Update export options.
     */
    fun updateExportOptions(options: ExportOptions) {
        _state.update { it.copy(exportOptions = options) }
    }

    /**
     * Update import options.
     */
    fun updateImportOptions(options: ImportOptions) {
        _state.update { it.copy(importOptions = options) }
    }

    /**
     * Update tile download options.
     */
    fun updateTileDownloadOptions(options: TileDownloadOptions) {
        _state.update { it.copy(tileDownloadOptions = options) }
    }

    /**
     * Show/hide dialogs.
     */
    fun showExportDialog() { _state.update { it.copy(showExportDialog = true) } }
    fun hideExportDialog() { _state.update { it.copy(showExportDialog = false) } }
    fun showImportDialog() { _state.update { it.copy(showImportDialog = true) } }
    fun hideImportDialog() { _state.update { it.copy(showImportDialog = false, selectedImportUri = null, importPreview = null) } }
    fun showImageExportDialog() { _state.update { it.copy(showImageExportDialog = true) } }
    fun hideImageExportDialog() { _state.update { it.copy(showImageExportDialog = false) } }
    fun showTileDownloadDialog() { _state.update { it.copy(showTileDownloadDialog = true) } }
    fun hideTileDownloadDialog() { _state.update { it.copy(showTileDownloadDialog = false) } }

    /**
     * Dismiss messages.
     */
    fun dismissMessage() {
        _state.update {
            it.copy(
                showSuccessMessage = false,
                showErrorMessage = false
            )
        }
    }
}
