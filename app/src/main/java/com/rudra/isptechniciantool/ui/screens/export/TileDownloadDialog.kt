package com.rudra.isptechniciantool.ui.screens.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.ui.theme.TechBlue
import com.rudra.isptechniciantool.util.TileDownloaderUtil

/**
 * Dialog for configuring offline tile download options.
 */
@Composable
fun TileDownloadDialog(
    options: TileDownloadOptions,
    estimation: TileDownloaderUtil.StorageEstimation?,
    onOptionsChange: (TileDownloadOptions) -> Unit,
    onDownload: (TileDownloaderUtil.TileBoundingBox) -> Unit,
    onDismiss: () -> Unit
) {
    var minZoom by remember { mutableStateOf(options.minZoom.toString()) }
    var maxZoom by remember { mutableStateOf(options.maxZoom.toString()) }
    var useCurrentMapView by remember { mutableStateOf(options.useCurrentMapView) }

    val minZoomInt = minZoom.toIntOrNull() ?: 10
    val maxZoomInt = maxZoom.toIntOrNull() ?: 17

    // Estimate tile count and size
    val estimatedTiles = remember(minZoomInt, maxZoomInt) {
        if (options.boundingBox != null) {
            TileDownloaderUtil.estimateTileCount(options.boundingBox, minZoomInt, maxZoomInt)
        } else 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = TechBlue
            )
        },
        title = {
            Text(
                text = stringResource(R.string.download_offline_tiles),
                color = TechBlue
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Information Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tile_download_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Zoom Levels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = minZoom,
                        onValueChange = { minZoom = it },
                        label = { Text(stringResource(R.string.min_zoom)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        supportingText = { Text(stringResource(R.string.range_10_17)) }
                    )
                    OutlinedTextField(
                        value = maxZoom,
                        onValueChange = { maxZoom = it },
                        label = { Text(stringResource(R.string.max_zoom)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        supportingText = { Text(stringResource(R.string.range_10_17)) }
                    )
                }

                // Use Current Map View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.use_current_map_view),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = useCurrentMapView,
                        onCheckedChange = { useCurrentMapView = it }
                    )
                }

                // Storage Estimation
                if (estimatedTiles > 0) {
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
                                text = stringResource(R.string.storage_estimation),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.estimated_tiles))
                                Text(
                                    text = estimatedTiles.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = TechBlue
                                )
                            }
                            if (estimation != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.estimated_size))
                                    Text(
                                        text = String.format("%.1f MB", estimation.estimatedSizeMB),
                                        fontWeight = FontWeight.Bold,
                                        color = TechBlue
                                    )
                                }
                            }
                        }
                    }
                }

                // Warning for large downloads
                if (estimatedTiles > 100000) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.large_download_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
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
                            minZoom = minZoomInt.coerceIn(1, 19),
                            maxZoom = maxZoomInt.coerceIn(1, 19),
                            useCurrentMapView = useCurrentMapView
                        )
                    )
                    if (options.boundingBox != null) {
                        onDownload(options.boundingBox)
                    } else {
                        // Use current map view will be handled by the ViewModel
                        onDownload(TileDownloaderUtil.TileBoundingBox(0.0, 0.0, 0.0, 0.0))
                    }
                },
                enabled = minZoomInt in 1..19 && maxZoomInt in 1..19 && minZoomInt <= maxZoomInt && estimatedTiles > 0
            ) {
                Text(stringResource(R.string.download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
