package com.rudra.isptechniciantool.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Map screen.
 * Manages device markers and map state.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    /**
     * Load all devices with coordinates.
     */
    fun loadDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val devices = deviceRepository.getAllDevices()
                    .filter { it.latitude != null && it.longitude != null }
                _uiState.update { 
                    it.copy(
                        devices = devices,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load devices"
                    )
                }
            }
        }
    }

    /**
     * Center map on a specific device.
     */
    fun centerOnDevice(device: Device) {
        device.latitude?.let { lat ->
            device.longitude?.let { lon ->
                _uiState.update {
                    it.copy(
                        mapCenterLatitude = lat,
                        mapCenterLongitude = lon,
                        zoomLevel = 18.0,
                        selectedDevice = device
                    )
                }
            }
        }
    }

    /**
     * Update map center position.
     */
    fun updateMapCenter(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                mapCenterLatitude = latitude,
                mapCenterLongitude = longitude
            )
        }
    }

    /**
     * Update zoom level.
     */
    fun updateZoomLevel(zoom: Double) {
        _uiState.update { it.copy(zoomLevel = zoom) }
    }

    /**
     * Toggle device labels visibility.
     */
    fun toggleLabels() {
        _uiState.update { it.copy(showDeviceLabels = !it.showDeviceLabels) }
    }

    /**
     * Select a device.
     */
    fun selectDevice(device: Device?) {
        _uiState.update { it.copy(selectedDevice = device) }
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
