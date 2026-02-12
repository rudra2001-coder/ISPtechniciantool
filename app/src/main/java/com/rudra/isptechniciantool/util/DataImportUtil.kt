package com.rudra.isptechniciantool.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkStatus
import com.rudra.isptechniciantool.domain.model.LinkType
import com.rudra.isptechniciantool.util.DataExportUtil.DeviceExport
import com.rudra.isptechniciantool.util.DataExportUtil.LinkExport
import java.io.File
import java.io.IOException

/**
 * Utility class for importing devices and links from JSON format.
 * Handles ID mapping, conflict resolution, and data validation.
 */
object DataImportUtil {

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    /**
     * Conflict resolution strategies for duplicate data.
     */
    enum class ConflictResolution {
        SKIP,      // Skip duplicate items
        OVERWRITE, // Replace existing items
        MERGE      // Merge fields (keep new non-null values)
    }

    /**
     * Import options for customizing the import process.
     */
    data class ImportOptions(
        val conflictResolution: ConflictResolution = ConflictResolution.SKIP,
        val validateData: Boolean = true,
        val preserveIds: Boolean = false,
        val remapIds: Boolean = true
    )

    /**
     * Result of an import operation.
     */
    data class ImportResult(
        val success: Boolean,
        val devicesImported: Int = 0,
        val devicesSkipped: Int = 0,
        val devicesFailed: Int = 0,
        val linksImported: Int = 0,
        val linksSkipped: Int = 0,
        val linksFailed: Int = 0,
        val errorMessages: List<String> = emptyList(),
        val importedDevices: List<Device> = emptyList(),
        val importedLinks: List<Link> = emptyList()
    )

    /**
     * Preview of import data without actually importing.
     */
    data class ImportPreview(
        val deviceCount: Int,
        val linkCount: Int,
        val devices: List<DataExportUtil.DeviceExport>,
        val links: List<DataExportUtil.LinkExport>,
        val validationErrors: List<String> = emptyList()
    )

    /**
     * Parses JSON string and returns ExportData.
     */
    fun parseJson(json: String): Result<DataExportUtil.ExportData> {
        return try {
            val exportData = gson.fromJson(json, DataExportUtil.ExportData::class.java)
            Result.success(exportData)
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        }
    }

    /**
     * Reads and parses JSON from a file.
     */
    fun parseFile(file: File): Result<DataExportUtil.ExportData> {
        return try {
            if (!file.exists()) {
                return Result.failure(IOException("File not found: ${file.absolutePath}"))
            }
            val json = file.readText()
            parseJson(json)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Validates a device export for import.
     */
    fun validateDevice(device: DataExportUtil.DeviceExport): List<String> {
        val errors = mutableListOf<String>()

        if (device.name.isBlank()) {
            errors.add("Device name cannot be empty")
        }

        if (device.deviceType.isBlank()) {
            errors.add("Device type cannot be empty")
        } else {
            try {
                DeviceType.valueOf(device.deviceType)
            } catch (e: IllegalArgumentException) {
                errors.add("Invalid device type: ${device.deviceType}")
            }
        }

        if (device.status.isBlank()) {
            errors.add("Device status cannot be empty")
        } else {
            try {
                DeviceStatus.valueOf(device.status)
            } catch (e: IllegalArgumentException) {
                errors.add("Invalid device status: ${device.status}")
            }
        }

        if (device.ipAddress != null && !isValidIpAddress(device.ipAddress)) {
            errors.add("Invalid IP address: ${device.ipAddress}")
        }

        return errors
    }

    /**
     * Validates a link export for import.
     */
    fun validateLink(link: DataExportUtil.LinkExport, deviceIds: Set<Long>): List<String> {
        val errors = mutableListOf<String>()

        if (link.sourceDeviceId == link.targetDeviceId) {
            errors.add("Link cannot connect device to itself")
        }

        if (!deviceIds.contains(link.sourceDeviceId)) {
            errors.add("Source device ID ${link.sourceDeviceId} not found in import data")
        }

        if (!deviceIds.contains(link.targetDeviceId)) {
            errors.add("Target device ID ${link.targetDeviceId} not found in import data")
        }

        if (link.linkType.isBlank()) {
            errors.add("Link type cannot be empty")
        } else {
            try {
                LinkType.valueOf(link.linkType)
            } catch (e: IllegalArgumentException) {
                errors.add("Invalid link type: ${link.linkType}")
            }
        }

        if (link.status.isBlank()) {
            errors.add("Link status cannot be empty")
        } else {
            try {
                LinkStatus.valueOf(link.status)
            } catch (e: IllegalArgumentException) {
                errors.add("Invalid link status: ${link.status}")
            }
        }

        return errors
    }

    /**
     * Validates IP address format.
     */
    private fun isValidIpAddress(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            val num = part.toIntOrNull() ?: return false
            num in 0..255
        }
    }

    /**
     * Generates a preview of import data.
     */
    fun previewImport(exportData: DataExportUtil.ExportData, options: ImportOptions = ImportOptions()): ImportPreview {
        val validationErrors = mutableListOf<String>()
        val deviceIds = exportData.devices.map { it.id }.toSet()

        if (options.validateData) {
            exportData.devices.forEach { device ->
                validateDevice(device).forEach { error ->
                    validationErrors.add("Device '${device.name}': $error")
                }
            }

            exportData.links.forEach { link ->
                validateLink(link, deviceIds).forEach { error ->
                    validationErrors.add("Link ${link.id}: $error")
                }
            }
        }

        return ImportPreview(
            deviceCount = exportData.devices.size,
            linkCount = exportData.links.size,
            devices = exportData.devices,
            links = exportData.links,
            validationErrors = validationErrors
        )
    }

    /**
     * Converts DeviceExport to Device domain model.
     */
    fun DeviceExport.toDomain(): Device {
        return Device(
            id = this.id,
            name = this.name,
            deviceType = try {
                DeviceType.valueOf(this.deviceType)
            } catch (e: IllegalArgumentException) {
                DeviceType.OTHER
            },
            ipAddress = this.ipAddress,
            macAddress = this.macAddress,
            latitude = this.latitude,
            longitude = this.longitude,
            topologyX = this.topologyX,
            topologyY = this.topologyY,
            status = try {
                DeviceStatus.valueOf(this.status)
            } catch (e: IllegalArgumentException) {
                DeviceStatus.UNKNOWN
            },
            monitoringEnabled = this.monitoringEnabled,
            pingInterval = this.pingInterval,
            lastSeen = this.lastSeen,
            notes = this.notes,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * Converts LinkExport to Link domain model.
     */
    fun LinkExport.toDomain(): Link {
        return Link(
            id = this.id,
            sourceDeviceId = this.sourceDeviceId,
            targetDeviceId = this.targetDeviceId,
            linkType = try {
                LinkType.valueOf(this.linkType)
            } catch (e: IllegalArgumentException) {
                LinkType.OTHER
            },
            bandwidth = this.bandwidth,
            status = try {
                LinkStatus.valueOf(this.status)
            } catch (e: IllegalArgumentException) {
                LinkStatus.UNKNOWN
            },
            color = this.color,
            createdAt = this.createdAt
        )
    }

    /**
     * Imports data with conflict resolution.
     */
    fun importData(
        exportData: DataExportUtil.ExportData,
        existingDeviceIds: Set<Long>,
        existingLinkIds: Set<Long>,
        options: ImportOptions = ImportOptions()
    ): ImportResult {
        val importedDevices = mutableListOf<Device>()
        val devicesSkipped = mutableListOf<Long>()
        val devicesFailed = mutableListOf<Long>()
        val errorMessages = mutableListOf<String>()

        val deviceIdMapping = mutableMapOf<Long, Long>()
        var nextDeviceId = (existingDeviceIds.maxOrNull() ?: 0L) + 1

        // Process devices
        exportData.devices.forEach { deviceExport ->
            val validationErrors = if (options.validateData) {
                validateDevice(deviceExport)
            } else emptyList()

            if (validationErrors.isNotEmpty()) {
                devicesFailed.add(deviceExport.id)
                errorMessages.addAll(validationErrors.map { "Device '${deviceExport.name}': $it" })
                return@forEach
            }

            val shouldImport = when (options.conflictResolution) {
                ConflictResolution.SKIP -> deviceExport.id !in existingDeviceIds
                ConflictResolution.OVERWRITE -> true
                ConflictResolution.MERGE -> true
            }

            if (shouldImport) {
                val newDevice = if (options.preserveIds) {
                    deviceExport.toDomain()
                } else {
                    val newId = if (options.remapIds && deviceExport.id in existingDeviceIds) {
                        val mappedId = nextDeviceId++
                        deviceIdMapping[deviceExport.id] = mappedId
                        mappedId
                    } else {
                        deviceExport.id
                    }
                    deviceExport.toDomain().copy(id = newId)
                }
                importedDevices.add(newDevice)
            } else {
                devicesSkipped.add(deviceExport.id)
            }
        }

        val importedLinks = mutableListOf<Link>()
        val linksSkipped = mutableListOf<Long>()
        val linksFailed = mutableListOf<Long>()

        val linkDeviceIds = (importedDevices.map { it.id } + exportData.devices.map { it.id }).toSet()

        // Process links
        exportData.links.forEach { linkExport ->
            val validationErrors = if (options.validateData) {
                validateLink(linkExport, linkDeviceIds)
            } else emptyList()

            if (validationErrors.isNotEmpty()) {
                linksFailed.add(linkExport.id)
                errorMessages.addAll(validationErrors.map { "Link ${linkExport.id}: $it" })
                return@forEach
            }

            val shouldImport = when (options.conflictResolution) {
                ConflictResolution.SKIP -> linkExport.id !in existingLinkIds
                ConflictResolution.OVERWRITE -> true
                ConflictResolution.MERGE -> true
            }

            if (shouldImport) {
                val newLink = if (options.preserveIds) {
                    linkExport.toDomain()
                } else {
                    val sourceId = deviceIdMapping[linkExport.sourceDeviceId] ?: linkExport.sourceDeviceId
                    val targetId = deviceIdMapping[linkExport.targetDeviceId] ?: linkExport.targetDeviceId
                    val newId = if (options.remapIds && linkExport.id in existingLinkIds) {
                        (existingLinkIds.maxOrNull() ?: 0L) + importedLinks.size + 1
                    } else {
                        linkExport.id
                    }
                    linkExport.toDomain().copy(
                        id = newId,
                        sourceDeviceId = sourceId,
                        targetDeviceId = targetId
                    )
                }
                importedLinks.add(newLink)
            } else {
                linksSkipped.add(linkExport.id)
            }
        }

        return ImportResult(
            success = errorMessages.isEmpty(),
            devicesImported = importedDevices.size,
            devicesSkipped = devicesSkipped.size,
            devicesFailed = devicesFailed.size,
            linksImported = importedLinks.size,
            linksSkipped = linksSkipped.size,
            linksFailed = linksFailed.size,
            errorMessages = errorMessages,
            importedDevices = importedDevices,
            importedLinks = importedLinks
        )
    }
}
