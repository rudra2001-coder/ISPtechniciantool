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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Main map screen with OSMDroid integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditDevice: (Long) -> Unit,
    onNavigateToTopology: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingAddLatitude by remember { mutableStateOf(0.0) }
    var pendingAddLongitude by remember { mutableStateOf(0.0) }
    var showLabels by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Map") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBlue,
                    titleContentColor = NeutralWhite,
                    actionIconContentColor = NeutralWhite
                ),
                actions = {
                    IconButton(onClick = { showLabels = !showLabels }) {
                        Icon(
                            imageVector = if (showLabels) Icons.Default.Label else Icons.Default.LabelOff,
                            contentDescription = "Toggle Labels"
                        )
                    }
                    IconButton(onClick = { viewModel.loadDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // My Location FAB
                FloatingActionButton(
                    onClick = { /* TODO: Center on user location */ },
                    containerColor = TechBlue,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
                
                // Add Device FAB
                FloatingActionButton(
                    onClick = { /* Show dialog to add device at current center */ },
                    containerColor = TechBlue
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Device")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map
            OsmMap(
                modifier = Modifier.fillMaxSize(),
                devices = uiState.devices,
                selectedDevice = uiState.selectedDevice,
                mapCenterLatitude = uiState.mapCenterLatitude,
                mapCenterLongitude = uiState.mapCenterLongitude,
                zoomLevel = uiState.zoomLevel,
                showDeviceLabels = showLabels,
                onMapCenterChanged = { lat, lon ->
                    viewModel.updateMapCenter(lat, lon)
                },
                onZoomChanged = { zoom ->
                    viewModel.updateZoomLevel(zoom)
                },
                onDeviceSelected = { device ->
                    viewModel.selectDevice(device)
                },
                onMapLongPress = { lat, lon ->
                    pendingAddLatitude = lat
                    pendingAddLongitude = lon
                    showAddDialog = true
                },
                onAddDeviceAtLocation = { lat, lon ->
                    pendingAddLatitude = lat
                    pendingAddLongitude = lon
                    showAddDialog = true
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TechBlue
                )
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Device detail bottom sheet
            uiState.selectedDevice?.let { device ->
                DeviceDetailBottomSheet(
                    device = device,
                    onDismiss = { viewModel.selectDevice(null) },
                    onEdit = { onNavigateToEditDevice(device.id) },
                    onDelete = { /* Handle delete */ },
                    onToggleMonitoring = { /* Handle monitoring toggle */ },
                    onNavigateToTopology = {
                        viewModel.selectDevice(null)
                        onNavigateToTopology()
                    }
                )
            }

            // Zoom controls overlay
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ZoomControls(
                    onZoomIn = { /* Handled by map multi-touch */ },
                    onZoomOut = { /* Handled by map multi-touch */ }
                )
            }
        }
    }

    // Add device dialog
    if (showAddDialog) {
        AddDeviceOnMapDialog(
            latitude = pendingAddLatitude,
            longitude = pendingAddLongitude,
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, ip, mac ->
                showAddDialog = false
                /* TODO: Add device with coordinates */
            }
        )
    }
}

@Composable
private fun ZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Column {
        Surface(
            shape = MaterialTheme.shapes.small,
            shadowElevation = 4.dp
        ) {
            Column {
                IconButton(onClick = onZoomIn) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                HorizontalDivider()
                IconButton(onClick = onZoomOut) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }
        }
    }
}
