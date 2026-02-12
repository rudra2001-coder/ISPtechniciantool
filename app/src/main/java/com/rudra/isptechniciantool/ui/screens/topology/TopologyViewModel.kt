package com.rudra.isptechniciantool.ui.screens.topology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkType
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import com.rudra.isptechniciantool.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Topology editor screen.
 */
@HiltViewModel
class TopologyViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TopologyState())
    val state: StateFlow<TopologyState> = _state.asStateFlow()

    init {
        loadTopology()
    }

    /**
     * Load all devices and links from repositories.
     */
    fun loadTopology() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                deviceRepository.observeAllDevices()
                    .combine(linkRepository.observeAllLinks()) { devices, links ->
                        _state.update { state ->
                            state.copy(
                                devices = devices,
                                links = links,
                                isLoading = false
                            )
                        }
                    }
                    .collect()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load topology"
                    )
                }
            }
        }
    }

    /**
     * Add a new device to the topology.
     */
    fun addDevice(
        name: String,
        deviceType: DeviceType,
        ipAddress: String? = null,
        macAddress: String? = null,
        topologyX: Float? = null,
        topologyY: Float? = null
    ) {
        viewModelScope.launch {
            try {
                val device = Device(
                    name = name,
                    deviceType = deviceType,
                    ipAddress = ipAddress,
                    macAddress = macAddress,
                    topologyX = topologyX,
                    topologyY = topologyY
                )
                deviceRepository.createDevice(device)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to add device") }
            }
        }
    }

    /**
     * Update device position on the canvas.
     */
    fun updateDevicePosition(deviceId: Long, x: Float, y: Float) {
        viewModelScope.launch {
            try {
                val device = deviceRepository.getDeviceById(deviceId)
                if (device != null) {
                    val updatedDevice = device.copy(
                        topologyX = x,
                        topologyY = y,
                        updatedAt = System.currentTimeMillis()
                    )
                    deviceRepository.updateDevice(updatedDevice)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to update device position") }
            }
        }
    }

    /**
     * Update an existing device.
     */
    fun updateDevice(device: Device) {
        viewModelScope.launch {
            try {
                deviceRepository.updateDevice(
                    device.copy(updatedAt = System.currentTimeMillis())
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to update device") }
            }
        }
    }

    /**
     * Delete a device.
     */
    fun deleteDevice(deviceId: Long) {
        viewModelScope.launch {
            try {
                deviceRepository.deleteDeviceById(deviceId)
                _state.update { 
                    if (it.selectedDeviceId == deviceId) {
                        it.copy(selectedDeviceId = null)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete device") }
            }
        }
    }

    /**
     * Create a new link between two devices.
     */
    fun createLink(
        sourceDeviceId: Long,
        targetDeviceId: Long,
        linkType: LinkType = LinkType.FIBER,
        bandwidth: String? = null,
        color: String = "#4CAF50"
    ) {
        viewModelScope.launch {
            try {
                val link = Link(
                    sourceDeviceId = sourceDeviceId,
                    targetDeviceId = targetDeviceId,
                    linkType = linkType,
                    bandwidth = bandwidth,
                    color = color
                )
                linkRepository.createLink(link)
                _state.update { it.copy(isAddLinkMode = false, linkSourceDeviceId = null) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to create link") }
            }
        }
    }

    /**
     * Delete a link.
     */
    fun deleteLink(linkId: Long) {
        viewModelScope.launch {
            try {
                linkRepository.deleteLinkById(linkId)
                _state.update { 
                    if (it.selectedLinkId == linkId) {
                        it.copy(selectedLinkId = null)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete link") }
            }
        }
    }

    /**
     * Select a device.
     */
    fun selectDevice(deviceId: Long?) {
        _state.update { currentState ->
            if (currentState.isAddLinkMode && deviceId != null) {
                if (currentState.linkSourceDeviceId == null) {
                    currentState.copy(linkSourceDeviceId = deviceId)
                } else if (currentState.linkSourceDeviceId != deviceId) {
                    currentState.copy(linkSourceDeviceId = deviceId)
                } else {
                    currentState
                }
            } else {
                currentState.copy(selectedDeviceId = deviceId, selectedLinkId = null)
            }
        }
    }

    /**
     * Select a link.
     */
    fun selectLink(linkId: Long?) {
        _state.update { it.copy(selectedLinkId = linkId, selectedDeviceId = null) }
    }

    /**
     * Clear all selections.
     */
    fun clearSelection() {
        _state.update { 
            it.copy(
                selectedDeviceId = null,
                selectedLinkId = null,
                isAddLinkMode = false,
                linkSourceDeviceId = null
            )
        }
    }

    /**
     * Start add link mode.
     */
    fun startAddLinkMode() {
        _state.update { 
            it.copy(
                isAddLinkMode = true,
                linkSourceDeviceId = null,
                selectedDeviceId = null,
                selectedLinkId = null
            )
        }
    }

    /**
     * Cancel add link mode.
     */
    fun cancelAddLinkMode() {
        _state.update { 
            it.copy(
                isAddLinkMode = false,
                linkSourceDeviceId = null
            )
        }
    }

    /**
     * Delete the currently selected item.
     */
    fun deleteSelected() {
        viewModelScope.launch {
            val currentState = _state.value
            when {
                currentState.selectedLinkId != null -> {
                    deleteLink(currentState.selectedLinkId)
                }
                currentState.selectedDeviceId != null -> {
                    deleteDevice(currentState.selectedDeviceId)
                }
            }
        }
    }

    /**
     * Update zoom level.
     */
    fun updateZoom(scale: Float) {
        _state.update { 
            it.copy(zoomScale = scale.coerceIn(0.5f, 3f)) 
        }
    }

    /**
     * Update pan offset.
     */
    fun updatePan(offsetX: Float, offsetY: Float) {
        _state.update { 
            it.copy(panOffsetX = offsetX, panOffsetY = offsetY) 
        }
    }

    /**
     * Handle device dragging.
     */
    fun startDrag(deviceId: Long, offsetX: Float, offsetY: Float) {
        _state.update { 
            it.copy(
                isDragging = true,
                draggedDeviceId = deviceId,
                dragOffsetX = offsetX,
                dragOffsetY = offsetY
            )
        }
    }

    /**
     * Handle drag movement.
     */
    fun onDrag(offsetX: Float, offsetY: Float) {
        _state.update { 
            it.copy(dragOffsetX = offsetX, dragOffsetY = offsetY) 
        }
    }

    /**
     * End drag and save position.
     */
    fun endDrag(deviceId: Long, finalX: Float, finalY: Float) {
        viewModelScope.launch {
            updateDevicePosition(deviceId, finalX, finalY)
            _state.update { 
                it.copy(
                    isDragging = false,
                    draggedDeviceId = null,
                    dragOffsetX = 0f,
                    dragOffsetY = 0f
                )
            }
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
