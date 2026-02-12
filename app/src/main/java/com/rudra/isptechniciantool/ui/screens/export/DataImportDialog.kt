package com.rudra.isptechniciantool.ui.screens.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.ui.theme.TechBlue
import com.rudra.isptechniciantool.util.DataImportUtil

/**
 * Dialog for configuring import options and previewing import data.
 */
@Composable
fun DataImportDialog(
    preview: DataImportUtil.ImportPreview,
    options: ImportOptions,
    onOptionsChange: (ImportOptions) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var conflictResolution by remember { mutableStateOf(options.conflictResolution) }
    var validateData by remember { mutableStateOf(options.validateData) }

    var preserveIds by remember { mutableStateOf(options.preserveIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null,
                tint = TechBlue
            )
        },
        title = {
            Text(
                text = stringResource(R.string.import_options),
                color = TechBlue
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.import_preview),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.devices_to_import))
                                Text(
                                    text = preview.deviceCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = TechBlue
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.links_to_import))
                                Text(
                                    text = preview.linkCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = TechBlue
                                )
                            }
                        }
                    }
                }

                // Validation Errors
                if (preview.validationErrors.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.validation_errors),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                preview.validationErrors.take(5).forEach { error ->
                                    Text(
                                        text = "• $error",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (preview.validationErrors.size > 5) {
                                    Text(
                                        text = stringResource(
                                            R.string.more_errors,
                                            preview.validationErrors.size - 5
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Conflict Resolution
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.conflict_resolution),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DataImportUtil.ConflictResolution.entries.forEach { resolution ->
                                FilterChip(
                                    selected = conflictResolution == resolution,
                                    onClick = { conflictResolution = resolution },
                                    label = {
                                        Text(
                                            when (resolution) {
                                                DataImportUtil.ConflictResolution.SKIP -> stringResource(R.string.skip)
                                                DataImportUtil.ConflictResolution.OVERWRITE -> stringResource(R.string.overwrite)
                                                DataImportUtil.ConflictResolution.MERGE -> stringResource(R.string.merge)
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Options Toggles
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.validate_data),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = validateData,
                                onCheckedChange = { validateData = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.preserve_ids),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = preserveIds,
                                onCheckedChange = { preserveIds = it }
                            )
                        }
                    }
                }

                // Resolution Description
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.resolution_description),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (conflictResolution) {
                                    DataImportUtil.ConflictResolution.SKIP -> stringResource(R.string.skip_description)
                                    DataImportUtil.ConflictResolution.OVERWRITE -> stringResource(R.string.overwrite_description)
                                    DataImportUtil.ConflictResolution.MERGE -> stringResource(R.string.merge_description)
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOptionsChange(
                        options.copy(
                            conflictResolution = conflictResolution,
                            validateData = validateData,
                            preserveIds = preserveIds
                        )
                    )
                    onConfirm()
                },
                enabled = preview.validationErrors.isEmpty() || !validateData
            ) {
                Text(stringResource(R.string.import_data))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
