package com.rudra.isptechniciantool.ui.screens.monitoring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.ui.theme.TechBlue

/**
 * Dialog for configuring global monitoring settings.
 */
@Composable
fun MonitoringSettingsDialog(
    currentSettings: MonitoringSettings,
    onDismiss: () -> Unit,
    onSave: (MonitoringSettings) -> Unit
) {
    var pingInterval by remember { mutableIntStateOf(currentSettings.defaultPingInterval) }
    var timeout by remember { mutableIntStateOf(currentSettings.defaultTimeout) }
    var alertThreshold by remember { mutableIntStateOf(currentSettings.alertThreshold) }
    var notificationsEnabled by remember { mutableStateOf(currentSettings.notificationsEnabled) }
    var autoRefreshEnabled by remember { mutableStateOf(currentSettings.autoRefreshEnabled) }
    var checkTcpPorts by remember { mutableStateOf(currentSettings.checkTcpPorts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = TechBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Monitoring Settings")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ping Interval
                OutlinedTextField(
                    value = pingInterval.toString(),
                    onValueChange = { value ->
                        pingInterval = value.toIntOrNull()?.coerceIn(5, 300) ?: 30
                    },
                    label = { Text("Ping Interval (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Range: 5-300 seconds") }
                )

                // Timeout
                OutlinedTextField(
                    value = timeout.toString(),
                    onValueChange = { value ->
                        timeout = value.toIntOrNull()?.coerceIn(500, 10000) ?: 2000
                    },
                    label = { Text("Timeout (milliseconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Range: 500-10000 ms") }
                )

                // Alert Threshold
                OutlinedTextField(
                    value = alertThreshold.toString(),
                    onValueChange = { value ->
                        alertThreshold = value.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    },
                    label = { Text("Alert Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Consecutive failures before alert (1-10)") }
                )

                HorizontalDivider()

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-refresh")
                    Switch(
                        checked = autoRefreshEnabled,
                        onCheckedChange = { autoRefreshEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Check TCP Ports")
                    Switch(
                        checked = checkTcpPorts,
                        onCheckedChange = { checkTcpPorts = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        MonitoringSettings(
                            defaultPingInterval = pingInterval,
                            defaultTimeout = timeout,
                            alertThreshold = alertThreshold,
                            notificationsEnabled = notificationsEnabled,
                            autoRefreshEnabled = autoRefreshEnabled,
                            checkTcpPorts = checkTcpPorts,
                            commonPortsToCheck = currentSettings.commonPortsToCheck
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
