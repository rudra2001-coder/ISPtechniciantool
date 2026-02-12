package com.rudra.isptechniciantool.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkStatus
import com.rudra.isptechniciantool.domain.model.LinkType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for exporting devices and links to JSON format.
 * Supports file writing and provides comprehensive data serialization.
 */
object DataExportUtil {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Export data class containing all exported information.
     */
    data class ExportData(
        val exportVersion: Int = 1,
        val exportDate: String = dateFormat.format(Date()),
        val devices: List<DeviceExport>,
        val links: List<LinkExport>,
        val metadata: ExportMetadata
    )

    data class DeviceExport(
        val id: Long,
        val name: String,
        val deviceType: String,
        val ipAddress: String?,
        val macAddress: String?,
        val latitude: Double?,
        val longitude: Double?,
        val topologyX: Float?,
        val topologyY: Float?,
        val status: String,
        val monitoringEnabled: Boolean,
        val pingInterval: Int,
        val lastSeen: Long?,
        val notes: String?,
        val createdAt: Long,
        val updatedAt: Long
    )

    data class LinkExport(
        val id: Long,
        val sourceDeviceId: Long,
        val targetDeviceId: Long,
        val linkType: String,
        val bandwidth: String?,
        val status: String,
        val color: String,
        val createdAt: Long
    )

    data class ExportMetadata(
        val deviceCount: Int,
        val linkCount: Int,
        val appVersion: String? = null,
        val description: String? = null
    )

    /**
     * Export options for customizing the export process.
     */
    data class ExportOptions(
        val includeDevices: Boolean = true,
        val includeLinks: Boolean = true,
        val includeMonitoringData: Boolean = true,
        val includeNotes: Boolean = true,
        val fileName: String? = null,
        val description: String? = null
    )

    /**
     * Converts a Device domain model to DeviceExport format.
     */
    fun Device.toExport(): DeviceExport {
        return DeviceExport(
            id = this.id,
            name = this.name,
            deviceType = this.deviceType.name,
            ipAddress = this.ipAddress,
            macAddress = this.macAddress,
            latitude = this.latitude,
            longitude = this.longitude,
            topologyX = this.topologyX,
            topologyY = this.topologyY,
            status = this.status.name,
            monitoringEnabled = this.monitoringEnabled,
            pingInterval = this.pingInterval,
            lastSeen = this.lastSeen,
            notes = this.notes,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * Converts a Link domain model to LinkExport format.
     */
    fun Link.toExport(): LinkExport {
        return LinkExport(
            id = this.id,
            sourceDeviceId = this.sourceDeviceId,
            targetDeviceId = this.targetDeviceId,
            linkType = this.linkType.name,
            bandwidth = this.bandwidth,
            status = this.status.name,
            color = this.color,
            createdAt = this.createdAt
        )
    }

    /**
     * Creates an ExportData object from devices and links.
     */
    fun createExportData(
        devices: List<Device>,
        links: List<Link>,
        options: ExportOptions = ExportOptions()
    ): ExportData {
        val deviceExports = if (options.includeDevices) {
            devices.map { device ->
                device.toExport().copy(
                    notes = if (options.includeNotes) device.notes else null
                )
            }
        } else emptyList()

        val linkExports = if (options.includeLinks) {
            links.map { link -> link.toExport() }
        } else emptyList()

        return ExportData(
            devices = deviceExports,
            links = linkExports,
            metadata = ExportMetadata(
                deviceCount = deviceExports.size,
                linkCount = linkExports.size,
                description = options.description
            )
        )
    }

    /**
     * Exports devices and links to a JSON string.
     */
    fun exportToJson(
        devices: List<Device>,
        links: List<Link>,
        options: ExportOptions = ExportOptions()
    ): String {
        val exportData = createExportData(devices, links, options)
        return gson.toJson(exportData)
    }

    /**
     * Exports data to a file and returns the file path.
     */
    fun exportToFile(
        devices: List<Device>,
        links: List<Link>,
        directory: File,
        options: ExportOptions = ExportOptions()
    ): Result<String> {
        return try {
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = options.fileName ?: "isp_backup_${dateFormat.format(Date())}.json"
            val file = File(directory, fileName)

            val json = exportToJson(devices, links, options)
            file.writeText(json)

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a default file name for export.
     */
    fun generateFileName(): String {
        return "isp_backup_${dateFormat.format(Date())}.json"
    }
}
