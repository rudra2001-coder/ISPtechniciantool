package com.rudra.isptechniciantool.ui.screens.topology

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.ui.components.getDeviceColor
import com.rudra.isptechniciantool.ui.theme.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Canvas composable for drawing the network topology.
 */
@Composable
fun TopologyCanvas(
    state: TopologyState,
    onDeviceClick: (Long) -> Unit,
    onDeviceDragStart: (Long, Float, Float) -> Unit,
    onDeviceDrag: (Float, Float) -> Unit,
    onDeviceDragEnd: (Long, Float, Float) -> Unit,
    onCanvasClick: () -> Unit,
    onZoom: (Float) -> Unit,
    onPan: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralWhite)
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onZoom(zoom)
                        onPan(pan.x, pan.y)
                    }
                }
                .pointerInput(state.devices, state.isDragging) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val (deviceId, _) = findDeviceAtPosition(
                                offset.x,
                                offset.y,
                                state.devices,
                                state.zoomScale,
                                state.panOffsetX,
                                state.panOffsetY
                            )
                            if (deviceId != null) {
                                onDeviceDragStart(deviceId, offset.x, offset.y)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDeviceDrag(dragAmount.x, dragAmount.y)
                        },
                        onDragEnd = {
                            val (deviceId, position) = findDeviceAtPosition(
                                state.dragOffsetX,
                                state.dragOffsetY,
                                state.devices,
                                state.zoomScale,
                                state.panOffsetX,
                                state.panOffsetY
                            )
                            if (deviceId != null) {
                                val x = position.first
                                val y = position.second
                                onDeviceDragEnd(deviceId, x, y)
                            }
                        }
                    )
                }
                .pointerInput(state.devices, state.isAddLinkMode, state.linkSourceDeviceId) {
                    detectTapGestures(
                        onTap = { offset ->
                            val (deviceId, _) = findDeviceAtPosition(
                                offset.x,
                                offset.y,
                                state.devices,
                                state.zoomScale,
                                state.panOffsetX,
                                state.panOffsetY
                            )
                            if (deviceId != null) {
                                onDeviceClick(deviceId)
                            } else {
                                onCanvasClick()
                            }
                        }
                    )
                }
        ) {
            // Apply transformations
            val scale = state.zoomScale
            val translateX = state.panOffsetX
            val translateY = state.panOffsetY
            
            // Draw grid background
            drawGrid(
                gridSize = 50f * scale,
                offsetX = translateX % (50f * scale),
                offsetY = translateY % (50f * scale),
                color = NeutralLightGray.copy(alpha = 0.3f)
            )
            
            // Draw links
            state.links.forEach { link ->
                val sourceDevice = state.devices.find { it.id == link.sourceDeviceId }
                val targetDevice = state.devices.find { it.id == link.targetDeviceId }
                if (sourceDevice != null && targetDevice != null) {
                    val sourcePos = getDeviceScreenPosition(
                        sourceDevice,
                        scale,
                        translateX,
                        translateY
                    )
                    val targetPos = getDeviceScreenPosition(
                        targetDevice,
                        scale,
                        translateX,
                        translateY
                    )
                    
                    drawLink(
                        link = link,
                        sourcePosition = sourcePos,
                        targetPosition = targetPos,
                        isSelected = link.id == state.selectedLinkId,
                        isSource = link.sourceDeviceId == state.linkSourceDeviceId,
                        textMeasurer = textMeasurer,
                        density = density,
                        surfaceColor = surfaceColor
                    )
                }
            }
            
            // Draw temporary link line if in add link mode
            if (state.isAddLinkMode && state.linkSourceDeviceId != null && state.dragOffsetX != 0f) {
                val sourceDevice = state.devices.find { it.id == state.linkSourceDeviceId }
                if (sourceDevice != null) {
                    val sourcePos = getDeviceScreenPosition(
                        sourceDevice,
                        scale,
                        translateX,
                        translateY
                    )
                    val targetPos = Offset(state.dragOffsetX, state.dragOffsetY)
                    
                    drawLine(
                        color = TechBlue.copy(alpha = 0.5f),
                        start = sourcePos,
                        end = targetPos,
                        strokeWidth = 2f
                    )
                }
            }
            
            // Draw devices
            state.devices.forEach { device ->
                val position = getDeviceScreenPosition(
                    device,
                    scale,
                    translateX,
                    translateY
                )
                
                val deviceRadius = 30f * scale
                
                drawDevice(
                    device = device,
                    position = position,
                    radius = deviceRadius,
                    isSelected = device.id == state.selectedDeviceId,
                    isSource = device.id == state.linkSourceDeviceId,
                    textMeasurer = textMeasurer,
                    density = density
                )
            }
        }
        
        // Zoom controls overlay
        ZoomControls(
            zoomScale = state.zoomScale,
            onZoomIn = { onZoom(1.1f) },
            onZoomOut = { onZoom(0.9f) },
            onResetView = { 
                onZoom(1f)
                onPan(-state.panOffsetX, -state.panOffsetY)
            },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

/**
 * Draw a device node on the canvas.
 */
private fun DrawScope.drawDevice(
    device: Device,
    position: Offset,
    radius: Float,
    isSelected: Boolean,
    isSource: Boolean,
    textMeasurer: TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    val deviceColor = getDeviceColor(device.deviceType)
    
    // Device circle
    drawCircle(
        color = deviceColor.copy(alpha = 0.2f),
        radius = radius,
        center = position
    )
    
    // Selection highlight
    if (isSelected) {
        drawCircle(
            color = TechBlue,
            radius = radius + 4f,
            center = position,
            style = Stroke(width = 3f)
        )
    }
    
    // Source highlight for link creation
    if (isSource) {
        drawCircle(
            color = SuccessGreen,
            radius = radius + 6f,
            center = position,
            style = Stroke(width = 2f)
        )
    }
    
    // Status indicator
    val statusColor = when (device.status.name) {
        "ONLINE" -> SuccessGreen
        "OFFLINE" -> ErrorRed
        "DEGRADED" -> WarningOrange
        else -> NeutralGray
    }
    
    drawCircle(
        color = statusColor,
        radius = radius * 0.2f,
        center = position + Offset(radius * 0.7f, -radius * 0.7f)
    )
}

/**
 * Draw a link between two devices.
 */
private fun DrawScope.drawLink(
    link: Link,
    sourcePosition: Offset,
    targetPosition: Offset,
    isSelected: Boolean,
    isSource: Boolean,
    textMeasurer: TextMeasurer,
    density: androidx.compose.ui.unit.Density,
    surfaceColor: Color
) {
    val linkColor = try {
        Color(android.graphics.Color.parseColor(link.color))
    } catch (e: Exception) {
        TechBlue
    }
    
    // Calculate control point for curved line
    val midPoint = (sourcePosition + targetPosition) / 2f
    val direction = targetPosition - sourcePosition
    val perpendicular = Offset(-direction.y, direction.x)
    val controlPointOffset = perpendicular * 0.2f
    val controlPoint1 = sourcePosition + controlPointOffset
    val controlPoint2 = targetPosition + controlPointOffset
    
    val path = Path().apply {
        moveTo(sourcePosition.x, sourcePosition.y)
        quadraticBezierTo(
            controlPoint1.x, controlPoint1.y,
            midPoint.x, midPoint.y
        )
        quadraticBezierTo(
            controlPoint2.x, controlPoint2.y,
            targetPosition.x, targetPosition.y
        )
    }
    
    // Draw link line
    drawPath(
        path = path,
        color = if (isSelected) TechBlue else linkColor,
        style = Stroke(
            width = if (isSelected) 4f else 2f
        )
    )
    
    // Draw link type indicator at midpoint
    drawCircle(
        color = surfaceColor,
        radius = 12f,
        center = midPoint
    )
    drawCircle(
        color = if (isSelected) TechBlue else linkColor,
        radius = 12f,
        center = midPoint,
        style = Stroke(width = 2f)
    )
}

/**
 * Draw a grid background.
 */
private fun DrawScope.drawGrid(
    gridSize: Float,
    offsetX: Float,
    offsetY: Float,
    color: Color
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    
    // Vertical lines
    var x = offsetX % gridSize
    while (x < canvasWidth) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, canvasHeight),
            strokeWidth = 1f
        )
        x += gridSize
    }
    
    // Horizontal lines
    var y = offsetY % gridSize
    while (y < canvasHeight) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = 1f
        )
        y += gridSize
    }
}

/**
 * Calculate screen position for a device.
 */
private fun getDeviceScreenPosition(
    device: Device,
    scale: Float,
    translateX: Float,
    translateY: Float
): Offset {
    val x = (device.topologyX ?: 400f) * scale + translateX
    val y = (device.topologyY ?: 300f) * scale + translateY
    return Offset(x, y)
}

/**
 * Find device at given screen position.
 */
private fun findDeviceAtPosition(
    screenX: Float,
    screenY: Float,
    devices: List<Device>,
    scale: Float,
    translateX: Float,
    translateY: Float
): Pair<Long?, Pair<Float, Float>> {
    val deviceRadius = 30f * scale
    
    for (device in devices) {
        val position = getDeviceScreenPosition(device, scale, translateX, translateY)
        val distance = sqrt(
            (screenX - position.x).pow(2) + 
            (screenY - position.y).pow(2)
        )
        
        if (distance <= deviceRadius) {
            // Convert screen position back to canvas coordinates
            val canvasX = (screenX - translateX) / scale
            val canvasY = (screenY - translateY) / scale
            return device.id to (canvasX to canvasY)
        }
    }
    
    return null to (0f to 0f)
}

/**
 * Zoom controls composable.
 */
@Composable
private fun ZoomControls(
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FloatingActionButton(
            onClick = onZoomIn,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In"
            )
        }
        
        FloatingActionButton(
            onClick = onZoomOut,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom Out"
            )
        }
        
        FloatingActionButton(
            onClick = onResetView,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Reset View"
            )
        }
        
        Text(
            text = "${(zoomScale * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
