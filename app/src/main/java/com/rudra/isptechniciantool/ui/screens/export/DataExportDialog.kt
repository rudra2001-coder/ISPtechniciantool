package com.rudra.isptechniciantool.ui.screens.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.ui.theme.TechBlue

/**
 * Dialog for configuring data export options.
 */
@Composable
fun DataExportDialog(
    options: ExportOptions,
    onOptionsChange: (ExportOptions) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var includeDevices by remember { mutableStateOf(options.includeDevices) }
    var includeLinks by remember { mutableStateOf(options.includeLinks) }
    var includeNotes by remember { mutableStateOf(options.includeNotes) }
    var fileName by remember { mutableStateOf(options.fileName) }
    var description by remember { mutableStateOf(options.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = TechBlue
            )
        },
        title = {
            Text(
                text = stringResource(R.string.export_options),
                color = TechBlue
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // File Name
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(stringResource(R.string.file_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                HorizontalDivider()

                // Include Devices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.include_devices))
                    Switch(
                        checked = includeDevices,
                        onCheckedChange = { includeDevices = it }
                    )
                }

                // Include Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.include_links))
                    Switch(
                        checked = includeLinks,
                        onCheckedChange = { includeLinks = it }
                    )
                }

                // Include Notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.include_notes))
                    Switch(
                        checked = includeNotes,
                        onCheckedChange = { includeNotes = it }
                    )
                }

                // Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.export_summary),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.will_export_devices_links,
                                if (includeDevices) "✓" else "✗",
                                if (includeLinks) "✓" else "✗"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOptionsChange(
                        options.copy(
                            includeDevices = includeDevices,
                            includeLinks = includeLinks,
                            includeNotes = includeNotes,
                            fileName = fileName,
                            description = description
                        )
                    )
                    onConfirm()
                },
                enabled = (includeDevices || includeLinks) && fileName.isNotBlank()
            ) {
                Text(stringResource(R.string.export))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
