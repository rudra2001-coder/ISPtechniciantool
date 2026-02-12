package com.rudra.isptechniciantool.ui.screens.topology

import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.Link

/**
 * Data class representing the UI state for the Topology screen.
 */
data class TopologyState(
    val devices: List<Device> = emptyList(),
    val links: List<Link> = emptyList(),
    val selectedDeviceId: Long? = null,
    val selectedLinkId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddLinkMode: Boolean = false,
    val linkSourceDeviceId: Long? = null,
    val zoomScale: Float = 1f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val isDragging: Boolean = false,
    val draggedDeviceId: Long? = null,
    val dragOffsetX: Float = 0f,
    val dragOffsetY: Float = 0f
) {
    val selectedDevice: Device?
        get() = devices.find { it.id == selectedDeviceId }
    
    val selectedLink: Link?
        get() = links.find { it.id == selectedLinkId }
    
    val hasSelection: Boolean
        get() = selectedDeviceId != null || selectedLinkId != null
}

/**
 * Events that can occur in the Topology screen.
 */
sealed class TopologyEvent {
    data class SelectDevice(val deviceId: Long?) : TopologyEvent()
    data class SelectLink(val linkId: Long?) : TopologyEvent()
    data class StartDrag(val deviceId: Long, val offsetX: Float, val offsetY: Float) : TopologyEvent()
    data class Dragging(val offsetX: Float, val offsetY: Float) : TopologyEvent()
    data class EndDrag(val deviceId: Long, val finalX: Float, val finalY: Float) : TopologyEvent()
    data class Zoom(val scale: Float) : TopologyEvent()
    data class Pan(val offsetX: Float, val offsetY: Float) : TopologyEvent()
    object ClearSelection : TopologyEvent()
    object StartAddLinkMode : TopologyEvent()
    object CancelAddLinkMode : TopologyEvent()
    data class SetLinkSource(val deviceId: Long) : TopologyEvent()
    object DeleteSelected : TopologyEvent()
    data class ShowError(val message: String) : TopologyEvent()
}
