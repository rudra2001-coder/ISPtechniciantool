package com.rudra.isptechniciantool.domain.model

/**
 * Domain model representing a network device in the topology.
 */
data class Device(
    val id: Long = 0,
    val name: String,
    val deviceType: DeviceType,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val topologyX: Float? = null,
    val topologyY: Float? = null,
    val status: DeviceStatus = DeviceStatus.UNKNOWN,
    val monitoringEnabled: Boolean = false,
    val pingInterval: Int = 60,
    val lastSeen: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing different types of network devices.
 */
enum class DeviceType {
    ROUTER,
    SWITCH,
    OLT,
    ONT,
    CPE,
    FIBER_NODE,
    WIFI_AP,
    FIREWALL,
    SERVER,
    UPS,
    OTHER
}

/**
 * Enum representing the status of a network device.
 */
enum class DeviceStatus {
    ONLINE,
    OFFLINE,
    UNKNOWN,
    DEGRADED,
    MAINTENANCE
}
