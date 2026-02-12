package com.rudra.isptechniciantool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Composable that displays a device with icon, name, and status indicator.
 */
@Composable
fun DeviceItem(
    device: Device,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, TechBlue, MaterialTheme.shapes.medium)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                TechLighterBlue.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon with Status Indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getDeviceColor(device.deviceType).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getDeviceIcon(device.deviceType)),
                    contentDescription = device.deviceType.name,
                    tint = getDeviceColor(device.deviceType),
                    modifier = Modifier.size(28.dp)
                )
                
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getStatusColor(device.status))
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Device Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = device.deviceType.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (device.ipAddress != null) {
                    Text(
                        text = device.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status Badge
            StatusBadge(status = device.status)
        }
    }
}

/**
 * Small status badge composable.
 */
@Composable
fun StatusBadge(
    status: DeviceStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = getStatusColor(status).copy(alpha = 0.1f)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Returns the appropriate drawable resource ID for a device type.
 */
fun getDeviceIcon(deviceType: DeviceType): Int {
    return when (deviceType) {
        DeviceType.ROUTER -> R.drawable.ic_device_router
        DeviceType.SWITCH -> R.drawable.ic_device_switch
        DeviceType.OLT -> R.drawable.ic_device_olt
        DeviceType.ONT -> R.drawable.ic_device_ont
        DeviceType.CPE -> R.drawable.ic_device_cpe
        DeviceType.FIBER_NODE -> R.drawable.ic_device_fiber_node
        DeviceType.WIFI_AP -> R.drawable.ic_device_wireless
        DeviceType.FIREWALL -> R.drawable.ic_device_router
        DeviceType.SERVER -> R.drawable.ic_device_server
        DeviceType.UPS -> R.drawable.ic_device_server
        DeviceType.OTHER -> R.drawable.ic_device_router
    }
}

/**
 * Returns the color associated with a device type.
 */
fun getDeviceColor(deviceType: DeviceType): Color {
    return when (deviceType) {
        DeviceType.ROUTER -> TechBlue
        DeviceType.SWITCH -> SuccessGreen
        DeviceType.OLT -> WarningOrange
        DeviceType.ONT -> InfoBlue
        DeviceType.CPE -> NeutralGray
        DeviceType.FIBER_NODE -> Purple
        DeviceType.WIFI_AP -> Cyan
        DeviceType.FIREWALL -> ErrorRed
        DeviceType.SERVER -> DarkGray
        DeviceType.UPS -> WarningOrange
        DeviceType.OTHER -> NeutralGray
    }
}

/**
 * Returns the color associated with a device status.
 */
fun getStatusColor(status: DeviceStatus): Color {
    return when (status) {
        DeviceStatus.ONLINE -> SuccessGreen
        DeviceStatus.OFFLINE -> ErrorRed
        DeviceStatus.UNKNOWN -> NeutralGray
        DeviceStatus.DEGRADED -> WarningOrange
        DeviceStatus.MAINTENANCE -> InfoBlue
    }
}
