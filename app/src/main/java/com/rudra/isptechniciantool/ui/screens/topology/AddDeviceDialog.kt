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
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.components.ISPButton
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Dialog for adding a new device to the topology.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        deviceType: DeviceType,
        ipAddress: String?,
        macAddress: String?,
        topologyX: Float?,
        topologyY: Float?
    ) -> Unit,
    initialX: Float? = null,
    initialY: Float? = null
) {
    var name by remember { mutableStateOf("") }
    var deviceType by remember { mutableStateOf(DeviceType.OTHER) }
    var ipAddress by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var topologyX by remember { mutableStateOf(initialX?.toString() ?: "") }
    var topologyY by remember { mutableStateOf(initialY?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    
    fun isValid(): Boolean = name.isNotBlank()
    
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
                    text = "Add Device",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                        singleLine = true,
                        enabled = initialX == null
                    )
                    
                    OutlinedTextField(
                        value = topologyY,
                        onValueChange = { topologyY = it },
                        label = { Text("Y Position") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = initialY == null
                    )
                }
                
                if (initialX != null && initialY != null) {
                    Text(
                        text = "Device will be placed at position ($initialX, $initialY)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechBlue,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
                        text = "Add Device",
                        onClick = {
                            if (!isValid()) {
                                nameError = true
                            } else {
                                onConfirm(
                                    name,
                                    deviceType,
                                    ipAddress.ifBlank { null },
                                    macAddress.ifBlank { null },
                                    topologyX.toFloatOrNull() ?: initialX ?: 400f,
                                    topologyY.toFloatOrNull() ?: initialY ?: 300f
                                )
                            }
                        },
                        enabled = isValid()
                    )
                }
            }
        }
    }
}
