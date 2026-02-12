package com.rudra.isptechniciantool.ui.screens.topology

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.ui.components.DeviceItem
import com.rudra.isptechniciantool.ui.components.EmptyState
import com.rudra.isptechniciantool.ui.components.LoadingIndicator

/**
 * Main topology editor screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopologyScreen(
    viewModel: TopologyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showEditDeviceDialog by remember { mutableStateOf(false) }
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showDeviceList by remember { mutableStateOf(false) }
    
    // Handle link source selection
    LaunchedEffect(state.linkSourceDeviceId, state.selectedDeviceId) {
        if (state.isAddLinkMode && state.linkSourceDeviceId != null && state.selectedDeviceId != null) {
            if (state.linkSourceDeviceId != state.selectedDeviceId) {
                showAddLinkDialog = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Network Topology")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Add Link mode indicator
                    if (state.isAddLinkMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Link Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.cancelAddLinkMode() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    IconButton(onClick = { showDeviceList = !showDeviceList }) {
                        Icon(
                            imageVector = if (showDeviceList) Icons.Default.ViewModule else Icons.Default.ViewList,
                            contentDescription = "Toggle Device List"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main FAB
                FloatingActionButton(
                    onClick = {
                        if (state.selectedDeviceId != null) {
                            showEditDeviceDialog = true
                        } else {
                            showAddDeviceDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (state.selectedDeviceId != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = if (state.selectedDeviceId != null) "Edit Device" else "Add Device"
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    // Add Link button
                    IconButton(
                        onClick = { viewModel.startAddLinkMode() },
                        enabled = state.devices.size >= 2
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Add Link"
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { viewModel.deleteSelected() },
                        enabled = state.hasSelection
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected"
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    // Clear selection
                    IconButton(
                        onClick = { viewModel.clearSelection() },
                        enabled = state.hasSelection
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Selection"
                        )
                    }
                },
                floatingActionButton = {
                    if (state.devices.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = { showAddDeviceDialog = true },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                            },
                            text = { Text("Add Device") }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator(
                        message = "Loading topology..."
                    )
                }
                
                state.devices.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.DeviceHub,
                        title = "No Devices",
                        message = "Add devices to start building your network topology",
                        actionText = "Add First Device",
                        onAction = { showAddDeviceDialog = true }
                    )
                }
                
                else -> {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Device List Sidebar
                        AnimatedVisibility(
                            visible = showDeviceList,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally() + fadeOut()
                        ) {
                            DeviceListSidebar(
                                devices = state.devices,
                                selectedDeviceId = state.selectedDeviceId,
                                onDeviceSelect = { deviceId ->
                                    viewModel.selectDevice(deviceId)
                                },
                                onDeviceEdit = { device ->
                                    viewModel.selectDevice(device.id)
                                    showEditDeviceDialog = true
                                },
                                modifier = Modifier
                                    .width(280.dp)
                                    .fillMaxHeight()
                            )
                        }
                        
                        // Topology Canvas
                        TopologyCanvas(
                            state = state,
                            onDeviceClick = { deviceId ->
                                if (state.isAddLinkMode) {
                                    viewModel.selectDevice(deviceId)
                                } else {
                                    viewModel.selectDevice(deviceId)
                                }
                            },
                            onDeviceDragStart = { deviceId, x, y ->
                                viewModel.startDrag(deviceId, x, y)
                            },
                            onDeviceDrag = { dx, dy ->
                                viewModel.onDrag(dx, dy)
                            },
                            onDeviceDragEnd = { deviceId, x, y ->
                                viewModel.endDrag(deviceId, x, y)
                            },
                            onCanvasClick = {
                                viewModel.clearSelection()
                            },
                            onZoom = { scale ->
                                viewModel.updateZoom(scale)
                            },
                            onPan = { x, y ->
                                viewModel.updatePan(x, y)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
            
            // Error Snackbar
            state.error?.let { error ->
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
        }
    }
    
    // Dialogs
    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onConfirm = { name, deviceType, ipAddress, macAddress, x, y ->
                viewModel.addDevice(name, deviceType, ipAddress, macAddress, x, y)
                showAddDeviceDialog = false
            }
        )
    }
    
    if (showEditDeviceDialog && state.selectedDevice != null) {
        EditDeviceDialog(
            device = state.selectedDevice!!,
            onDismiss = { showEditDeviceDialog = false },
            onConfirm = { device ->
                viewModel.updateDevice(device)
                showEditDeviceDialog = false
            },
            onDelete = {
                viewModel.deleteDevice(state.selectedDeviceId!!)
                showEditDeviceDialog = false
            }
        )
    }
    
    if (showAddLinkDialog) {
        AddLinkDialog(
            devices = state.devices,
            sourceDeviceId = state.linkSourceDeviceId,
            targetDeviceId = state.selectedDeviceId,
            onDismiss = {
                showAddLinkDialog = false
                viewModel.cancelAddLinkMode()
            },
            onConfirm = { sourceId, targetId, linkType, bandwidth, color ->
                viewModel.createLink(sourceId, targetId, linkType, bandwidth, color)
                showAddLinkDialog = false
            }
        )
    }
}

/**
 * Device list sidebar component.
 */
@Composable
private fun DeviceListSidebar(
    devices: List<Device>,
    selectedDeviceId: Long?,
    onDeviceSelect: (Long) -> Unit,
    onDeviceEdit: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Devices",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${devices.size} devices",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Device List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceItem(
                        device = device,
                        isSelected = device.id == selectedDeviceId,
                        onClick = { onDeviceSelect(device.id) }
                    )
                }
            }
        }
    }
}
