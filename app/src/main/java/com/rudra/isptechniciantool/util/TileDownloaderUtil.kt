package com.rudra.isptechniciantool.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

/**
 * Utility class for downloading OSMDroid tiles for offline use.
 * Supports bounding box selection, progress callbacks, and cache management.
 */
object TileDownloaderUtil {

    /**
     * Download options for customizing the tile download process.
     */
    data class DownloadOptions(
        val minZoom: Int = 10,
        val maxZoom: Int = 17,
        val threadCount: Int = 2,
        val includeOverlayTiles: Boolean = true,
        val overwriteExisting: Boolean = false
    )

    /**
     * Bounding box for tile download area.
     */
    data class TileBoundingBox(
        val north: Double,
        val south: Double,
        val east: Double,
        val west: Double
    ) {
        fun toOsmBoundingBox(): BoundingBox {
            return BoundingBox(north, east, south, west)
        }

        companion object {
            fun fromMapView(mapView: MapView, paddingPercent: Double = 0.1): TileBoundingBox {
                val boundingBox = mapView.boundingBox
                val latSpan = boundingBox.latitudeSpan
                val lonSpan = boundingBox.longitudeSpan

                val latPadding = latSpan * paddingPercent
                val lonPadding = lonSpan * paddingPercent

                return TileBoundingBox(
                    north = boundingBox.latNorth + latPadding,
                    south = boundingBox.latSouth - latPadding,
                    east = boundingBox.lonEast + lonPadding,
                    west = boundingBox.lonWest - lonPadding
                )
            }

            fun fromGeoPoints(points: List<GeoPoint>): TileBoundingBox {
                val lats = points.map { it.latitude }
                val lons = points.map { it.longitude }

                return TileBoundingBox(
                    north = lats.maxOrNull() ?: 0.0,
                    south = lats.minOrNull() ?: 0.0,
                    east = lons.maxOrNull() ?: 0.0,
                    west = lons.minOrNull() ?: 0.0
                )
            }
        }
    }

    /**
     * Download progress callback.
     */
    interface DownloadProgressCallback {
        fun onProgressUpdate(current: Int, total: Int, percentage: Int)
        fun onDownloadComplete(totalDownloaded: Int)
        fun onError(error: String)
    }

    /**
     * Storage estimation result.
     */
    data class StorageEstimation(
        val tileCount: Long,
        val estimatedSizeBytes: Long,
        val estimatedSizeMB: Double
    ) {
        companion object {
            private const val AVERAGE_TILE_SIZE_BYTES = 15000L // ~15KB per tile

            fun calculate(tileCount: Long): StorageEstimation {
                val estimatedSize = tileCount * AVERAGE_TILE_SIZE_BYTES
                return StorageEstimation(
                    tileCount = tileCount,
                    estimatedSizeBytes = estimatedSize,
                    estimatedSizeMB = estimatedSize / (1024.0 * 1024.0)
                )
            }
        }
    }

    /**
     * Estimates the number of tiles for a bounding box and zoom range.
     */
    fun estimateTileCount(
        boundingBox: TileBoundingBox,
        minZoom: Int,
        maxZoom: Int
    ): Long {
        var totalTiles = 0L

        for (zoom in minZoom..maxZoom) {
            val tilesAtZoom = calculateTilesAtZoom(boundingBox, zoom)
            totalTiles += tilesAtZoom
        }

        return totalTiles
    }

    /**
     * Calculates the number of tiles at a specific zoom level.
     */
    private fun calculateTilesAtZoom(boundingBox: TileBoundingBox, zoom: Int): Long {
        val n = Math.pow(2.0, zoom.toDouble())

        val northTile = (n * ((1.0 - Math.log(Math.tan(Math.toRadians(boundingBox.north)) +
                1.0 / Math.cos(Math.toRadians(boundingBox.north))) / Math.PI) / 2.0)).toInt()
        val southTile = (n * ((1.0 - Math.log(Math.tan(Math.toRadians(boundingBox.south)) +
                1.0 / Math.cos(Math.toRadians(boundingBox.south))) / Math.PI) / 2.0)).toInt()
        val eastTile = ((boundingBox.east + 180.0) / 360.0 * n).toInt()
        val westTile = ((boundingBox.west + 180.0) / 360.0 * n).toInt()

        val xTiles = (eastTile - westTile + 1).coerceAtLeast(1)
        val yTiles = (northTile - southTile + 1).coerceAtLeast(1)

        return (xTiles * yTiles).toLong()
    }

    /**
     * Estimates storage for a bounding box and zoom range.
     */
    fun estimateStorage(
        boundingBox: TileBoundingBox,
        options: DownloadOptions
    ): StorageEstimation {
        val tileCount = estimateTileCount(boundingBox, options.minZoom, options.maxZoom)
        return StorageEstimation.calculate(tileCount)
    }

    /**
     * Downloads tiles for a bounding box.
     */
    suspend fun downloadTiles(
        context: Context,
        mapView: MapView,
        boundingBox: TileBoundingBox,
        options: DownloadOptions = DownloadOptions(),
        callback: DownloadProgressCallback? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Configure OSMDroid
            val config = Configuration.getInstance()
            config.userAgentValue = context.packageName

            // Use CacheManager for downloading tiles
            val cacheManager = CacheManager(mapView)

            val osmBoundingBox = boundingBox.toOsmBoundingBox()

            // Calculate total tiles for progress
            val totalTiles = estimateTileCount(boundingBox, options.minZoom, options.maxZoom)
            var downloadedTiles = 0

            // Download tiles using CacheManager
            cacheManager.downloadAreaAsync(
                context,
                osmBoundingBox,
                options.minZoom,
                options.maxZoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        callback?.onDownloadComplete(downloadedTiles)
                    }

                    override fun updateProgress(
                        progress: Int,
                        currentZoomLevel: Int,
                        zoomMin: Int,
                        zoomMax: Int
                    ) {
                        downloadedTiles = progress
                        val percentage = if (totalTiles > 0) {
                            (progress * 100 / totalTiles.toInt())
                        } else 0
                        callback?.onProgressUpdate(progress, totalTiles.toInt(), percentage)
                    }

                    override fun downloadStarted() {
                        // Download started
                    }

                    override fun setPossibleTilesInArea(total: Int) {
                        // Area tiles set
                    }

                    override fun onTaskFailed(errors: Int) {
                        callback?.onError("Download failed with $errors errors")
                    }
                }
            )

            Result.success(downloadedTiles)

            Result.success(downloadedTiles)
        } catch (e: Exception) {
            callback?.onError(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Exports downloaded tiles to a file for backup/sharing.
     */
    suspend fun exportTilesToFile(
        context: Context,
        mapView: MapView,
        boundingBox: TileBoundingBox,
        minZoom: Int,
        maxZoom: Int,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val writer = SqliteArchiveTileWriter(outputFile.absolutePath)

            val cacheManager = CacheManager(mapView, writer)

            cacheManager.downloadAreaAsync(
                context,
                boundingBox.toOsmBoundingBox(),
                minZoom,
                maxZoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        // Export complete
                    }

                    override fun updateProgress(
                        progress: Int,
                        currentZoomLevel: Int,
                        zoomMin: Int,
                        zoomMax: Int
                    ) {
                        // Progress update
                    }

                    override fun downloadStarted() {
                        // Download started
                    }

                    override fun setPossibleTilesInArea(total: Int) {
                        // Area tiles set
                    }

                    override fun onTaskFailed(errors: Int) {
                        // Task failed
                    }
                }
            )

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the current cache size.
     */
    suspend fun getCacheSize(context: Context): Long = withContext(Dispatchers.IO) {
        val config = Configuration.getInstance()
        val cacheDir = File(config.osmdroidTileCache.absolutePath)

        if (cacheDir.exists()) {
            calculateDirSize(cacheDir)
        } else {
            0L
        }
    }

    /**
     * Calculates the size of a directory.
     */
    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirSize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    /**
     * Clears the tile cache.
     */
    suspend fun clearCache(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val config = Configuration.getInstance()
            val cacheDir = File(config.osmdroidTileCache.absolutePath)

            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets a maximum cache size limit.
     */
    fun setCacheLimit(context: Context, maxSizeMB: Long) {
        val config = Configuration.getInstance()
        config.tileFileSystemCacheMaxBytes = maxSizeMB * 1024 * 1024
    }

    /**
     * Gets the OSMDroid tile cache directory.
     */
    fun getCacheDirectory(context: Context): File {
        val config = Configuration.getInstance()
        return config.osmdroidTileCache
    }

    /**
     * Formats file size for display.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
