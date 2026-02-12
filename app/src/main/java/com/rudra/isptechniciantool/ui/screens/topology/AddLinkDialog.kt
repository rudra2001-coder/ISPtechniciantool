package com.rudra.isptechniciantool.ui.screens.topology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.LinkType
import com.rudra.isptechniciantool.ui.components.DeviceItem
import com.rudra.isptechniciantool.ui.components.ISPButton
import com.rudra.isptechniciantool.ui.theme.ErrorRed

/**
 * Dialog for creating a link between two devices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkDialog(
    devices: List<Device>,
    sourceDeviceId: Long?,
    targetDeviceId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (
        sourceDeviceId: Long,
        targetDeviceId: Long,
        linkType: LinkType,
        bandwidth: String?,
        color: String
    ) -> Unit
) {
    var selectedLinkType by remember { mutableStateOf(LinkType.FIBER) }
    var bandwidth by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }
    var expanded by remember { mutableStateOf(false) }
    var showSourceSelector by remember { mutableStateOf(sourceDeviceId == null) }
    var showTargetSelector by remember { mutableStateOf(targetDeviceId == null) }
    
    val sourceDevice = devices.find { it.id == sourceDeviceId }
    val targetDevice = devices.find { it.id == targetDeviceId }
    
    fun isValid(): Boolean = sourceDeviceId != null && targetDeviceId != null && sourceDeviceId != targetDeviceId
    
    val availableColors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#FF9800" to "Orange",
        "#9C27B0" to "Purple",
        "#F44336" to "Red",
        "#607D8B" to "Gray",
        "#795548" to "Brown",
        "#00BCD4" to "Cyan"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Create Link",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Source Device
                Text(
                    text = "Source Device *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sourceDevice != null) {
                    DeviceItem(
                        device = sourceDevice,
                        isSelected = true,
                        onClick = { showSourceSelector = true }
                    )
                } else {
                    OutlinedButton(
                        onClick = { showSourceSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Source Device")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Target Device
                Text(
                    text = "Target Device *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (targetDevice != null) {
                    DeviceItem(
                        device = targetDevice,
                        isSelected = true,
                        onClick = { showTargetSelector = true }
                    )
                } else {
                    OutlinedButton(
                        onClick = { showTargetSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Target Device")
                    }
                }
                
                // Validation message
                if (sourceDeviceId == targetDeviceId && sourceDeviceId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Source and target devices cannot be the same",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Link Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedLinkType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LinkType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedLinkType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bandwidth
                OutlinedTextField(
                    value = bandwidth,
                    onValueChange = { bandwidth = it },
                    label = { Text("Bandwidth (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    placeholder = { Text("e.g., 1Gbps, 10Gbps") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Color Picker
                Text(
                    text = "Link Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableColors.forEach { (colorValue, colorName) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorValue)))
                                .then(
                                    if (selectedColor == colorValue) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = colorValue }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    ISPButton(
                        text = "Create Link",
                        onClick = {
                            if (isValid() && sourceDeviceId != null && targetDeviceId != null) {
                                onConfirm(
                                    sourceDeviceId,
                                    targetDeviceId,
                                    selectedLinkType,
                                    bandwidth.ifBlank { null },
                                    selectedColor
                                )
                            }
                        },
                        enabled = isValid()
                    )
                }
            }
        }
    }
    
    // Device Selector Dialog
    if (showSourceSelector || showTargetSelector) {
        DeviceSelectorDialog(
            devices = devices,
            excludeDeviceId = if (showTargetSelector && targetDeviceId != null) targetDeviceId else null,
            onSelect = { device ->
                if (showSourceSelector) {
                    showSourceSelector = false
                    // Will be handled by parent via state
                } else {
                    showTargetSelector = false
                    // Will be handled by parent via state
                }
            },
            onDismiss = {
                showSourceSelector = false
                showTargetSelector = false
            }
        )
    }
}

/**
 * Dialog for selecting a device.
 */
@Composable
private fun DeviceSelectorDialog(
    devices: List<Device>,
    excludeDeviceId: Long? = null,
    onSelect: (Device) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Device",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (devices.isEmpty()) {
                    Text(
                        text = "No devices available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(devices.filter { it.id != excludeDeviceId }) { device ->
                            DeviceItem(
                                device = device,
                                onClick = { onSelect(device) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
