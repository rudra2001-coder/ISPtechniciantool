package com.rudra.isptechniciantool.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskPriority
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.screens.dashboard.TaskPriorityBadge
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Dashboard screen displaying key metrics and quick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCustomerList: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToRouterSettings: () -> Unit,
    onNavigateToNetworkTools: () -> Unit,
    onNavigateToTaskList: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSecrets: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ISP Technician Tool",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBlue,
                    titleContentColor = NeutralWhite,
                    actionIconContentColor = NeutralWhite
                ),
                actions = {
                    IconButton(onClick = onNavigateToRouterSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Router Settings")
                    }
                    IconButton(onClick = onNavigateToSecrets) {
                        Icon(Icons.Default.Key, contentDescription = "Secrets")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = TechBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Stats Section
                item {
                    SectionHeader(title = "Overview")
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Customers",
                            value = uiState.totalCustomers.toString(),
                            icon = Icons.Default.Groups,
                            iconTint = TechBlue,
                            subtitle = "All customers"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Active",
                            value = uiState.activeCustomers.toString(),
                            icon = Icons.Default.Person,
                            iconTint = SuccessGreen,
                            subtitle = "Currently active"
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Pending Sync",
                            value = uiState.pendingSyncCount.toString(),
                            icon = Icons.Default.Pending,
                            iconTint = WarningOrange,
                            subtitle = "Awaiting sync"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Failed",
                            value = uiState.failedSyncCount.toString(),
                            icon = Icons.Default.Error,
                            iconTint = ErrorRed,
                            subtitle = "Sync errors"
                        )
                    }
                }
                
                // Sync Status Card
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = CardType.ELEVATED
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Data Synchronization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isSyncing) "Syncing data..." 
                                           else "${uiState.pendingSyncCount} items pending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (uiState.pendingSyncCount > 0 && !uiState.isSyncing) {
                                ISPButton(
                                    text = "Sync Now",
                                    onClick = { viewModel.syncNow() },
                                    loading = uiState.isSyncing,
                                    buttonType = ButtonType.PRIMARY,
                                    height = 40.dp
                                )
                            } else if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = TechBlue,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                StatusBadge(
                                    text = "Up to date",
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
                
                // Quick Actions Section
                item {
                    SectionHeader(title = "Quick Actions")
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Customers",
                            icon = Icons.Default.Groups,
                            count = uiState.totalCustomers,
                            onClick = onNavigateToCustomerList
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Network Tools",
                            icon = Icons.Default.NetworkCheck,
                            count = null,
                            onClick = onNavigateToNetworkTools
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Tasks",
                            icon = Icons.Default.Task,
                            count = if (uiState.pendingTasks.isNotEmpty()) uiState.pendingTasks.size else null,
                            onClick = onNavigateToTaskList
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Backup",
                            icon = Icons.Default.Backup,
                            count = null,
                            onClick = onNavigateToBackup
                        )
                    }
                }
                
                // Recent Customers Section
                if (uiState.recentCustomers.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Recent Customers",
                            action = {
                                TextButton(onClick = onNavigateToCustomerList) {
                                    Text("View All", color = TechBlue)
                                }
                            }
                        )
                    }
                    
                    items(uiState.recentCustomers) { customer ->
                        RecentCustomerItem(customer = customer)
                    }
                }
                
                // Pending Tasks Section
                if (uiState.pendingTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Pending Tasks",
                            action = {
                                TextButton(onClick = onNavigateToTaskList) {
                                    Text("View All", color = TechBlue)
                                }
                            }
                        )
                    }
                    
                    items(uiState.pendingTasks.take(3)) { task ->
                        PendingTaskItem(task = task)
                    }
                }
                
                // Router Status Section
                item {
                    SectionHeader(title = "System Status")
                }
                
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = if (uiState.isRouterConfigured) CardType.ELEVATED else CardType.OUTLINED,
                        onClick = onNavigateToRouterSettings
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.isRouterConfigured) Icons.Default.Router else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (uiState.isRouterConfigured) SuccessGreen else ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (uiState.isRouterConfigured) "Router Connected" else "Router Not Configured",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    uiState.routerName?.let { routerName ->
                                        Text(
                                            text = routerName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TechBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (count != null && count > 0) {
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentCustomerItem(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = TechBlue,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            SyncStatusBadge(syncStatus = customer.syncStatus)
        }
    }
}

@Composable
private fun SyncStatusBadge(syncStatus: SyncStatus) {
    val (text, color) = when (syncStatus) {
        SyncStatus.SYNCED -> "Synced" to SuccessGreen
        SyncStatus.PENDING -> "Pending" to WarningOrange
        SyncStatus.FAILED -> "Failed" to ErrorRed
        SyncStatus.SYNCING -> "Syncing" to TechBlue
        else -> syncStatus.name to NeutralGray
    }
    
    StatusBadge(text = text, color = color)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingTaskItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (task.description != null) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            TaskPriorityBadge(priority = task.priority.name)
        }
    }
}

@Composable
private fun TaskPriorityBadge(priority: String) {
    val color = when (priority.lowercase()) {
        "high", "urgent" -> ErrorRed
        "medium" -> WarningOrange
        "low" -> SuccessGreen
        else -> NeutralGray
    }
    
    StatusBadge(text = priority, color = color)
}
