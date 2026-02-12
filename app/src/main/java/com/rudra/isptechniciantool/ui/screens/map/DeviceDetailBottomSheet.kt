package com.rudra.isptechniciantool.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Bottom sheet showing device details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailBottomSheet(
    device: Device,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleMonitoring: (Boolean) -> Unit,
    onNavigateToTopology: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = getStatusColor(device.status).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp),
                            tint = getStatusColor(device.status)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = device.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = getStatusColor(device.status)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device name and type
            Text(
                text = device.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = device.deviceType.name.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Device info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (device.ipAddress != null) {
                        InfoRow(
                            icon = Icons.Default.SettingsEthernet,
                            label = "IP Address",
                            value = device.ipAddress
                        )
                    }

                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Coordinates",
                        value = "${device.latitude?.let { "%.6f".format(it) }} / ${device.longitude?.let { "%.6f".format(it) }}"
                    )

                    if (device.macAddress != null) {
                        InfoRow(
                            icon = Icons.Default.Memory,
                            label = "MAC Address",
                            value = device.macAddress
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monitoring toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = if (device.monitoringEnabled) TechBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Monitoring")
                }
                Switch(
                    checked = device.monitoringEnabled,
                    onCheckedChange = onToggleMonitoring
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = onNavigateToTopology,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeviceHub, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Topology")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete button
            OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Device")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Device") },
            text = { Text("Are you sure you want to delete \"${device.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun getStatusColor(status: DeviceStatus) = when (status) {
    DeviceStatus.ONLINE -> SuccessGreen
    DeviceStatus.OFFLINE -> ErrorRed
    DeviceStatus.UNKNOWN -> WarningOrange
    DeviceStatus.DEGRADED -> WarningOrange
    DeviceStatus.MAINTENANCE -> TechBlue
}
