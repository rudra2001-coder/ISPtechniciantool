package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.model.DeviceType

/**
 * Room entity representing a network device in the topology.
 */
@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "device_type")
    val deviceType: String,
    
    @ColumnInfo(name = "ip_address")
    val ipAddress: String? = null,
    
    @ColumnInfo(name = "mac_address")
    val macAddress: String? = null,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "topology_x")
    val topologyX: Float? = null,
    
    @ColumnInfo(name = "topology_y")
    val topologyY: Float? = null,
    
    @ColumnInfo(name = "status")
    val status: String = DeviceStatus.UNKNOWN.name,
    
    @ColumnInfo(name = "monitoring_enabled")
    val monitoringEnabled: Boolean = false,
    
    @ColumnInfo(name = "ping_interval")
    val pingInterval: Int = 60,
    
    @ColumnInfo(name = "last_seen")
    val lastSeen: Long? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Device {
        return Device(
            id = id,
            name = name,
            deviceType = DeviceType.valueOf(deviceType),
            ipAddress = ipAddress,
            macAddress = macAddress,
            latitude = latitude,
            longitude = longitude,
            topologyX = topologyX,
            topologyY = topologyY,
            status = DeviceStatus.valueOf(status),
            monitoringEnabled = monitoringEnabled,
            pingInterval = pingInterval,
            lastSeen = lastSeen,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomainModel(device: Device): DeviceEntity {
            return DeviceEntity(
                id = device.id,
                name = device.name,
                deviceType = device.deviceType.name,
                ipAddress = device.ipAddress,
                macAddress = device.macAddress,
                latitude = device.latitude,
                longitude = device.longitude,
                topologyX = device.topologyX,
                topologyY = device.topologyY,
                status = device.status.name,
                monitoringEnabled = device.monitoringEnabled,
                pingInterval = device.pingInterval,
                lastSeen = device.lastSeen,
                notes = device.notes,
                createdAt = device.createdAt,
                updatedAt = device.updatedAt
            )
        }
    }
}
