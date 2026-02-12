package com.rudra.isptechniciantool.ui.screens.map

import com.rudra.isptechniciantool.domain.model.Device

/**
 * Data class representing the UI state for the Map screen.
 */
data class MapState(
    val devices: List<Device> = emptyList(),
    val selectedDevice: Device? = null,
    val mapCenterLatitude: Double = 0.0,
    val mapCenterLongitude: Double = 0.0,
    val zoomLevel: Double = 15.0,
    val showDeviceLabels: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
