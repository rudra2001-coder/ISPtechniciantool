package com.rudra.isptechniciantool.ui.screens.monitoring

import com.rudra.isptechniciantool.domain.model.Device

/**
 * UI state for the Monitoring screen.
 */
data class MonitoringState(
    val monitoredDevices: List<MonitoredDevice> = emptyList(),
    val monitoringStatus: MonitoringStatus = MonitoringStatus.IDLE,
    val lastUpdateTimestamp: Long? = null,
    val overallNetworkStatus: OverallNetworkStatus = OverallNetworkStatus.UNKNOWN,
    val alertCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val settings: MonitoringSettings = MonitoringSettings()
)

/**
 * Represents a device being monitored.
 */
data class MonitoredDevice(
    val device: Device,
    val currentStatus: DeviceMonitorStatus = DeviceMonitorStatus.UNKNOWN,
    val latency: Long? = null,
    val lastChecked: Long? = null,
    val isMonitoringEnabled: Boolean = true,
    val tcpPortsReachable: List<Int> = emptyList(),
    val consecutiveFailures: Int = 0
)

/**
 * Monitoring status for the overall monitoring session.
 */
enum class MonitoringStatus {
    IDLE,      // Monitoring not started
    RUNNING,   // Actively monitoring devices
    PAUSED     // Monitoring paused
}

/**
 * Overall network status based on all monitored devices.
 */
enum class OverallNetworkStatus {
    ONLINE,    // All devices online
    DEGRADED,  // Some devices offline or degraded
    OFFLINE,   // All devices offline
    UNKNOWN    // No data available
}

/**
 * Status of an individual monitored device.
 */
enum class DeviceMonitorStatus {
    ONLINE,    // Device is reachable
    OFFLINE,   // Device is not reachable
    DEGRADED,  // Device partially reachable (some ports down)
    UNKNOWN,   // Status not yet determined
    CHECKING,  // Currently checking status
    ERROR      // Error checking status
}

/**
 * Monitoring settings configuration.
 */
data class MonitoringSettings(
    val defaultPingInterval: Int = 30,      // Seconds between pings
    val defaultTimeout: Int = 2000,          // Milliseconds timeout
    val alertThreshold: Int = 3,              // Consecutive failures before alert
    val notificationsEnabled: Boolean = true,
    val autoRefreshEnabled: Boolean = true,
    val checkTcpPorts: Boolean = true,
    val commonPortsToCheck: List<Int> = listOf(22, 80, 443)
)

/**
 * Summary statistics for the monitoring dashboard.
 */
data class MonitoringSummary(
    val totalDevices: Int = 0,
    val onlineDevices: Int = 0,
    val offlineDevices: Int = 0,
    val degradedDevices: Int = 0,
    val unknownDevices: Int = 0,
    val averageLatency: Long? = null
) {
    val healthPercentage: Int
        get() = if (totalDevices > 0) {
            ((onlineDevices + degradedDevices).toFloat() / totalDevices * 100).toInt()
        } else 0
}
