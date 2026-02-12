package com.rudra.isptechniciantool.ui.screens.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.ui.components.CardType
import com.rudra.isptechniciantool.ui.components.ISPCard
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.theme.NeutralWhite
import com.rudra.isptechniciantool.ui.theme.TechBlue
import kotlinx.coroutines.launch
import org.osmdroid.views.MapView

/**
 * Export/Import management screen for data and image export, and offline tile download.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    onExportTopologyImage: ((Bitmap) -> Unit) -> Unit,
    onDownloadTiles: ((MapView) -> Unit) -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()

    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.previewImport(it) }
    }

    // Snackbar messages
    LaunchedEffect(state.showSuccessMessage, state.showErrorMessage) {
        if (state.showSuccessMessage) {
            snackbarHostState.showSnackbar("Operation completed successfully")
            viewModel.dismissMessage()
        } else if (state.showErrorMessage) {
            snackbarHostState.showSnackbar(state.importError ?: state.exportError ?: state.imageExportError ?: state.tileDownloadError ?: "An error occurred")
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_backup), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBlue,
                    titleContentColor = NeutralWhite,
                    navigationIconContentColor = NeutralWhite
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Summary Card
            item {

             ISPCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardType = CardType.ELEVATED
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.data_summary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.devices),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = state.totalDevices.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TechBlue
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.links),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = state.totalLinks.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TechBlue
                                )
                            }
                        }
                    }
                }
            }

            // Data Export Section
            item {
                SectionHeader(title = stringResource(R.string.data_export))
            }

            item {
               ISPCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardType = CardType.ELEVATED
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.export_to_json),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.export_to_json_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.showExportDialog() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isExporting && state.totalDevices > 0
                        ) {
                            if (state.isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeutralWhite
                                )
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.export_data))
                        }
                        if (state.exportSuccess) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.getShareIntent()?.let { context.startActivity(it) } }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.share))
                            }
                        }
                    }
                }
            }

            // Data Import Section
            item {
                SectionHeader(title = stringResource(R.string.data_import))
            }

            item {
              ISPCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardType = CardType.ELEVATED
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.import_from_json),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.import_from_json_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isImporting
                        ) {
                            if (state.isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeutralWhite
                                )
                            } else {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.import_data))
                        }
                    }
                }
            }

            // Image Export Section
            item {
                SectionHeader(title = stringResource(R.string.image_export))
            }

            item {
                ISPCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardType = CardType.ELEVATED
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.export_topology_image),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.export_topology_image_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onExportTopologyImage { bitmap ->
                                    viewModel.exportImage(bitmap)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isImageExporting && state.totalDevices > 0
                        ) {
                            if (state.isImageExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeutralWhite
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.export_image))
                        }
                        if (state.imageExportSuccess) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.getImageShareIntent()?.let { context.startActivity(it) } }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.share))
                            }
                        }
                    }
                }
            }

            // Offline Tiles Section
            item {
                SectionHeader(title = stringResource(R.string.offline_tiles))
            }

            item {
              ISPCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardType = CardType.ELEVATED
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.download_offline_tiles),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.download_offline_tiles_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.cache_size),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (state.isLoadingCacheSize) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = state.cacheSize,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.showTileDownloadDialog() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isTileDownloading
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.download))
                            }
                            OutlinedButton(
                                onClick = { viewModel.clearCache() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isTileDownloading
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.clear))
                            }
                        }
                        if (state.isTileDownloading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { state.tileDownloadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${state.downloadedTileCount} tiles downloaded",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (state.showExportDialog) {
        DataExportDialog(
            options = state.exportOptions,
            onOptionsChange = { viewModel.updateExportOptions(it) },
            onConfirm = {
                viewModel.exportData()
                viewModel.hideExportDialog()
            },
            onDismiss = { viewModel.hideExportDialog() }
        )
    }

    if (state.showImportDialog && state.importPreview != null) {
        DataImportDialog(
            preview = state.importPreview!!,
            options = state.importOptions,
            onOptionsChange = { viewModel.updateImportOptions(it) },
            onConfirm = {
                viewModel.importData()
                viewModel.hideImportDialog()
            },
            onDismiss = { viewModel.hideImportDialog() }
        )
    }

    if (state.showTileDownloadDialog) {
        TileDownloadDialog(
            options = state.tileDownloadOptions,
            estimation = state.tileEstimation,
            onOptionsChange = { viewModel.updateTileDownloadOptions(it) },
            onDownload = { boundingBox ->
                viewModel.hideTileDownloadDialog()
                onDownloadTiles { mapView ->
                    viewModel.downloadTiles(mapView)
                }
            },
            onDismiss = { viewModel.hideTileDownloadDialog() }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TechBlue,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
