package com.rudra.isptechniciantool.ui.screens.topology

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.components.ISPButton
import com.rudra.isptechniciantool.ui.components.ISPDialog
import com.rudra.isptechniciantool.ui.theme.ErrorRed

/**
 * Dialog for editing an existing device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceDialog(
    device: Device,
    onDismiss: () -> Unit,
    onConfirm: (Device) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(device.name) }
    var deviceType by remember { mutableStateOf(device.deviceType) }
    var ipAddress by remember { mutableStateOf(device.ipAddress ?: "") }
    var macAddress by remember { mutableStateOf(device.macAddress ?: "") }
    var topologyX by remember { mutableStateOf(device.topologyX?.toString() ?: "") }
    var topologyY by remember { mutableStateOf(device.topologyY?.toString() ?: "") }
    var monitoringEnabled by remember { mutableStateOf(device.monitoringEnabled) }
    var expanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    fun isValid(): Boolean = name.isNotBlank()
    
    if (showDeleteConfirm) {
        ISPDialog(
            title = "Delete Device",
            message = "Are you sure you want to delete \"${device.name}\"? This will also delete all associated links.",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
            isDestructive = true
        )
    } else {
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
                        text = "Edit Device",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "ID: ${device.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text("Device Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Name is required") }
                        } else null,
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = deviceType.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Device Type") },
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
                            DeviceType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ")) },
                                    onClick = {
                                        deviceType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // IP Address
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // MAC Address
                    OutlinedTextField(
                        value = macAddress,
                        onValueChange = { macAddress = it },
                        label = { Text("MAC Address") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Position fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = topologyX,
                            onValueChange = { topologyX = it },
                            label = { Text("X Position") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = topologyY,
                            onValueChange = { topologyY = it },
                            label = { Text("Y Position") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Monitoring toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = monitoringEnabled,
                            onCheckedChange = { monitoringEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Monitoring",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${device.status.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "Last Seen: ${device.lastSeen?.let {
                                java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(it))
                            } ?: "Never"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = ErrorRed
                            )
                        ) {
                            Text("Delete")
                        }
                        
                        Row {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            ISPButton(
                                text = "Save",
                                onClick = {
                                    if (!isValid()) {
                                        nameError = true
                                    } else {
                                        val updatedDevice = device.copy(
                                            name = name,
                                            deviceType = deviceType,
                                            ipAddress = ipAddress.ifBlank { null },
                                            macAddress = macAddress.ifBlank { null },
                                            topologyX = topologyX.toFloatOrNull(),
                                            topologyY = topologyY.toFloatOrNull(),
                                            monitoringEnabled = monitoringEnabled
                                        )
                                        onConfirm(updatedDevice)
                                    }
                                },
                                enabled = isValid()
                            )
                        }
                    }
                }
            }
        }
    }
}
