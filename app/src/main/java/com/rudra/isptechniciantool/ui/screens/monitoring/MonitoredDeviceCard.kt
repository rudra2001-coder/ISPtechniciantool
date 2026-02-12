package com.rudra.isptechniciantool.ui.screens.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable card displaying a monitored device with its status.
 */
@Composable
fun MonitoredDeviceCard(
    monitoredDevice: MonitoredDevice,
    onToggleMonitoring: () -> Unit,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val device = monitoredDevice.device

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device info and status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status indicator
                    StatusIndicator(status = monitoredDevice.currentStatus)

                    Spacer(modifier = Modifier.width(12.dp))

                    // Device icon and info
                    DeviceIcon(deviceType = device.deviceType)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        device.ipAddress?.let { ip ->
                            Text(
                                text = ip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Monitoring toggle
                Switch(
                    checked = monitoredDevice.isMonitoringEnabled,
                    onCheckedChange = { onToggleMonitoring() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TechBlue,
                        checkedTrackColor = TechBlue.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                StatusBadge(status = monitoredDevice.currentStatus)

                // Latency display
                monitoredDevice.latency?.let { latency ->
                    LatencyDisplay(latency = latency)
                } ?: Spacer(modifier = Modifier.width(1.dp))
            }

            // TCP ports section
            if (monitoredDevice.tcpPortsReachable.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TcpPortsRow(ports = monitoredDevice.tcpPortsReachable)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last checked time
            monitoredDevice.lastChecked?.let { timestamp ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last checked: ${formatTimestamp(timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onRefresh,
                    enabled = monitoredDevice.currentStatus != DeviceMonitorStatus.CHECKING
                ) {
                    if (monitoredDevice.currentStatus == DeviceMonitorStatus.CHECKING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: DeviceMonitorStatus) {
    val (color, icon) = when (status) {
        DeviceMonitorStatus.ONLINE -> SuccessGreen to Icons.Default.CheckCircle
        DeviceMonitorStatus.OFFLINE -> ErrorRed to Icons.Default.Cancel
        DeviceMonitorStatus.DEGRADED -> WarningOrange to Icons.Default.Warning
        DeviceMonitorStatus.UNKNOWN -> NeutralGray to Icons.Default.Help
        DeviceMonitorStatus.CHECKING -> TechBlue to Icons.Default.Pending
        DeviceMonitorStatus.ERROR -> ErrorRed to Icons.Default.Error
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun StatusBadge(status: DeviceMonitorStatus) {
    val (text, color) = when (status) {
        DeviceMonitorStatus.ONLINE -> "Online" to SuccessGreen
        DeviceMonitorStatus.OFFLINE -> "Offline" to ErrorRed
        DeviceMonitorStatus.DEGRADED -> "Degraded" to WarningOrange
        DeviceMonitorStatus.UNKNOWN -> "Unknown" to NeutralGray
        DeviceMonitorStatus.CHECKING -> "Checking..." to TechBlue
        DeviceMonitorStatus.ERROR -> "Error" to ErrorRed
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DeviceIcon(deviceType: DeviceType) {
    val icon = when (deviceType) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.SWITCH -> Icons.Default.SettingsEthernet
        DeviceType.OLT -> Icons.Default.Speed
        DeviceType.ONT -> Icons.Default.Wifi
        DeviceType.CPE -> Icons.Default.WifiTethering
        DeviceType.FIBER_NODE -> Icons.Default.SignalCellularAlt
        DeviceType.WIFI_AP -> Icons.Default.NetworkWifi
        DeviceType.FIREWALL -> Icons.Default.Security
        DeviceType.SERVER -> Icons.Default.Dns
        DeviceType.UPS -> Icons.Default.BatteryChargingFull
        DeviceType.OTHER -> Icons.Default.DeviceHub
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(TechBlue.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = deviceType.name,
            tint = TechBlue,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LatencyDisplay(latency: Long) {
    val color = when {
        latency < 50 -> SuccessGreen
        latency < 100 -> WarningOrange
        else -> ErrorRed
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${latency}ms",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TcpPortsRow(ports: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ports.forEach { port ->
            PortBadge(port = port)
        }
    }
}

@Composable
private fun PortBadge(port: Int) {
    val (name, color) = when (port) {
        22 -> "SSH" to SuccessGreen
        23 -> "Telnet" to WarningOrange
        80 -> "HTTP" to TechBlue
        443 -> "HTTPS" to TechBlue
        161 -> "SNMP" to WarningOrange
        8728 -> "API" to Purple500
        else -> ":$port" to NeutralGray
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
