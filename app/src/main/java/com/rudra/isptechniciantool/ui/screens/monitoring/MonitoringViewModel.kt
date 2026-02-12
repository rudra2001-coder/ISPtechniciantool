package com.rudra.isptechniciantool.ui.screens.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import com.rudra.isptechniciantool.domain.repository.LinkRepository
import com.rudra.isptechniciantool.util.PingUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Monitoring screen.
 * Manages device monitoring state and periodic connectivity checks.
 */
@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringState())
    val uiState: StateFlow<MonitoringState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadMonitoredDevices()
    }

    /**
     * Load devices that have monitoring enabled.
     */
    fun loadMonitoredDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val devices = deviceRepository.getDevicesForMonitoring()
                val monitoredDevices = devices.map { device ->
                    MonitoredDevice(
                        device = device,
                        currentStatus = if (device.status != DeviceStatus.UNKNOWN) {
                            mapDeviceStatusToMonitorStatus(device.status)
                        } else {
                            DeviceMonitorStatus.UNKNOWN
                        },
                        latency = null,
                        lastChecked = device.lastSeen,
                        isMonitoringEnabled = device.monitoringEnabled,
                        consecutiveFailures = 0
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        monitoredDevices = monitoredDevices,
                        isLoading = false,
                        overallNetworkStatus = calculateOverallStatus(monitoredDevices)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load devices: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Start monitoring all enabled devices.
     */
    fun startMonitoring() {
        if (_uiState.value.monitoringStatus == MonitoringStatus.RUNNING) {
            return
        }

        _uiState.update { it.copy(monitoringStatus = MonitoringStatus.RUNNING) }
        startPeriodicRefresh()
        refreshAllDevices()
    }

    /**
     * Stop monitoring all devices.
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        refreshJob?.cancel()
        _uiState.update { it.copy(monitoringStatus = MonitoringStatus.IDLE) }
    }

    /**
     * Pause monitoring without stopping.
     */
    fun pauseMonitoring() {
        refreshJob?.cancel()
        _uiState.update { it.copy(monitoringStatus = MonitoringStatus.PAUSED) }
    }

    /**
     * Toggle monitoring for a specific device.
     */
    fun toggleDeviceMonitoring(deviceId: Long) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedDevices = state.monitoredDevices.map { monitoredDevice ->
                    if (monitoredDevice.device.id == deviceId) {
                        val newEnabled = !monitoredDevice.isMonitoringEnabled
                        // Update in repository
                        viewModelScope.launch {
                            deviceRepository.getDeviceById(deviceId)?.let { device ->
                                deviceRepository.updateDevice(
                                    device.copy(
                                        monitoringEnabled = newEnabled,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                        monitoredDevice.copy(isMonitoringEnabled = newEnabled)
                    } else {
                        monitoredDevice
                    }
                }
                state.copy(monitoredDevices = updatedDevices)
            }
        }
    }

    /**
     * Manually refresh status for all devices.
     */
    fun refreshStatus() {
        if (_uiState.value.monitoringStatus != MonitoringStatus.RUNNING) {
            startMonitoring()
        } else {
            refreshAllDevices()
        }
    }

    /**
     * Refresh status for a single device.
     */
    fun refreshDeviceStatus(deviceId: Long) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedDevices = state.monitoredDevices.map { monitoredDevice ->
                    if (monitoredDevice.device.id == deviceId &&
                        monitoredDevice.device.ipAddress != null
                    ) {
                        monitoredDevice.copy(currentStatus = DeviceMonitorStatus.CHECKING)
                    } else {
                        monitoredDevice
                    }
                }
                state.copy(monitoredDevices = updatedDevices)
            }

            checkDevice(deviceId)
        }
    }

    /**
     * Update monitoring settings.
     */
    fun updateSettings(settings: MonitoringSettings) {
        _uiState.update { it.copy(settings = settings) }
    }

    /**
     * Start periodic refresh of device statuses.
     */
    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(_uiState.value.settings.defaultPingInterval * 1000L)
                if (_uiState.value.monitoringStatus == MonitoringStatus.RUNNING) {
                    refreshAllDevices()
                }
            }
        }
    }

    /**
     * Refresh all monitored devices.
     */
    private fun refreshAllDevices() {
        val enabledDevices = _uiState.value.monitoredDevices
            .filter { it.isMonitoringEnabled }
            .map { it.device.id }

        if (enabledDevices.isEmpty()) {
            _uiState.update {
                it.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    overallNetworkStatus = OverallNetworkStatus.UNKNOWN
                )
            }
            return
        }

        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            for (deviceId in enabledDevices) {
                checkDevice(deviceId)
                // Small delay between checks to avoid network congestion
                delay(100)
            }
            _uiState.update { state ->
                state.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    overallNetworkStatus = calculateOverallStatus(state.monitoredDevices),
                    alertCount = state.monitoredDevices.count {
                        it.consecutiveFailures >= _uiState.value.settings.alertThreshold
                    }
                )
            }
        }
    }

    /**
     * Check connectivity for a specific device.
     */
    private suspend fun checkDevice(deviceId: Long) {
        val device = _uiState.value.monitoredDevices
            .find { it.device.id == deviceId }
            ?.device ?: return

        val ipAddress = device.ipAddress ?: return

        val connectivityResult = PingUtil.checkDeviceConnectivity(
            ipAddress = ipAddress,
            pingTimeout = _uiState.value.settings.defaultTimeout,
            tcpTimeout = _uiState.value.settings.defaultTimeout
        )

        val newStatus = when (connectivityResult.overallStatus) {
            PingUtil.ConnectivityStatus.ONLINE -> DeviceMonitorStatus.ONLINE
            PingUtil.ConnectivityStatus.DEGRADED -> DeviceMonitorStatus.DEGRADED
            PingUtil.ConnectivityStatus.OFFLINE -> DeviceMonitorStatus.OFFLINE
            PingUtil.ConnectivityStatus.UNREACHABLE -> DeviceMonitorStatus.ERROR
        }

        val previousFailures = _uiState.value.monitoredDevices
            .find { it.device.id == deviceId }
            ?.consecutiveFailures ?: 0

        val newFailures = if (newStatus == DeviceMonitorStatus.ONLINE) {
            0
        } else {
            previousFailures + 1
        }

        // Update device status in repository
        val repositoryStatus = when (newStatus) {
            DeviceMonitorStatus.ONLINE -> DeviceStatus.ONLINE
            DeviceMonitorStatus.OFFLINE -> DeviceStatus.OFFLINE
            DeviceMonitorStatus.DEGRADED -> DeviceStatus.DEGRADED
            else -> DeviceStatus.UNKNOWN
        }

        deviceRepository.updateDeviceStatus(
            deviceId = deviceId,
            status = repositoryStatus.name,
            lastSeen = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val updatedDevices = state.monitoredDevices.map { monitoredDevice ->
                if (monitoredDevice.device.id == deviceId) {
                    monitoredDevice.copy(
                        currentStatus = newStatus,
                        latency = connectivityResult.bestLatency,
                        lastChecked = System.currentTimeMillis(),
                        tcpPortsReachable = connectivityResult.reachablePorts,
                        consecutiveFailures = newFailures
                    )
                } else {
                    monitoredDevice
                }
            }
            state.copy(
                monitoredDevices = updatedDevices,
                overallNetworkStatus = calculateOverallStatus(updatedDevices)
            )
        }
    }

    /**
     * Calculate overall network status from monitored devices.
     */
    private fun calculateOverallStatus(devices: List<MonitoredDevice>): OverallNetworkStatus {
        if (devices.isEmpty()) return OverallNetworkStatus.UNKNOWN

        val onlineCount = devices.count {
            it.currentStatus == DeviceMonitorStatus.ONLINE
        }
        val offlineCount = devices.count {
            it.currentStatus == DeviceMonitorStatus.OFFLINE
        }
        val degradedCount = devices.count {
            it.currentStatus == DeviceMonitorStatus.DEGRADED
        }

        return when {
            onlineCount == devices.size -> OverallNetworkStatus.ONLINE
            offlineCount == devices.size -> OverallNetworkStatus.OFFLINE
            onlineCount + degradedCount > 0 -> OverallNetworkStatus.DEGRADED
            else -> OverallNetworkStatus.UNKNOWN
        }
    }

    /**
     * Map domain DeviceStatus to MonitorStatus.
     */
    private fun mapDeviceStatusToMonitorStatus(status: DeviceStatus): DeviceMonitorStatus {
        return when (status) {
            DeviceStatus.ONLINE -> DeviceMonitorStatus.ONLINE
            DeviceStatus.OFFLINE -> DeviceMonitorStatus.OFFLINE
            DeviceStatus.DEGRADED -> DeviceMonitorStatus.DEGRADED
            DeviceStatus.UNKNOWN -> DeviceMonitorStatus.UNKNOWN
            DeviceStatus.MAINTENANCE -> DeviceMonitorStatus.DEGRADED
        }
    }

    /**
     * Get monitoring summary statistics.
     */
    fun getSummary(): MonitoringSummary {
        val devices = _uiState.value.monitoredDevices
        return MonitoringSummary(
            totalDevices = devices.size,
            onlineDevices = devices.count { it.currentStatus == DeviceMonitorStatus.ONLINE },
            offlineDevices = devices.count { it.currentStatus == DeviceMonitorStatus.OFFLINE },
            degradedDevices = devices.count { it.currentStatus == DeviceMonitorStatus.DEGRADED },
            unknownDevices = devices.count {
                it.currentStatus == DeviceMonitorStatus.UNKNOWN ||
                it.currentStatus == DeviceMonitorStatus.ERROR
            },
            averageLatency = devices
                .mapNotNull { it.latency }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toLong()
        )
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
        refreshJob?.cancel()
    }
}
