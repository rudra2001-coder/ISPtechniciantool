package com.rudra.isptechniciantool.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskStatus
import java.time.format.DateTimeFormatter

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
                title = { Text("ISP Technician Tool") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToRouterSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Router Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary
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
                // Summary Cards Section
                item {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Customers",
                            value = uiState.totalCustomers.toString(),
                            icon = Icons.Default.Groups,
                            onClick = onNavigateToCustomerList
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Active",
                            value = uiState.activeCustomers.toString(),
                            icon = Icons.Default.Person,
                            onClick = onNavigateToCustomerList
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Pending Sync",
                            value = uiState.pendingSyncCount.toString(),
                            icon = Icons.Default.Pending,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToCustomerList
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Failed Sync",
                            value = uiState.failedSyncCount.toString(),
                            icon = Icons.Default.Error,
                            iconTint = MaterialTheme.colorScheme.error,
                            onClick = onNavigateToCustomerList
                        )
                    }
                }
                
                // Quick Actions Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SyncActionCard(
                            modifier = Modifier.weight(1f),
                            pendingSyncCount = uiState.pendingSyncCount,
                            isSyncing = uiState.isSyncing,
                            isRouterConfigured = uiState.isRouterConfigured,
                            onClick = { viewModel.syncNow() }
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Network Tools",
                            icon = Icons.Default.NetworkCheck,
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
                            badge = if (uiState.overdueTaskCount > 0) uiState.overdueTaskCount else null,
                            onClick = onNavigateToTaskList
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Backup",
                            icon = Icons.Default.Backup,
                            onClick = onNavigateToBackup
                        )
                    }
                }
                
                // Recent Activity Section
                if (uiState.recentCustomers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recent Customers",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.recentCustomers) { customer ->
                        RecentCustomerItem(customer = customer)
                    }
                }
                
                // Pending Tasks Section
                if (uiState.pendingTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pending Tasks",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.pendingTasks.take(3)) { task ->
                        PendingTaskItem(task = task)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    badge: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (badge != null && badge > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                BadgeCount(count = badge)
            }
        }
    }
}

@Composable
private fun SyncActionCard(
    modifier: Modifier = Modifier,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    isRouterConfigured: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(enabled = !isSyncing && isRouterConfigured && pendingSyncCount > 0, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSyncing -> MaterialTheme.colorScheme.secondaryContainer
                !isRouterConfigured -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    isSyncing -> "Syncing..."
                    !isRouterConfigured -> "Configure Router"
                    pendingSyncCount == 0 -> "Sync ($pendingSyncCount)"
                    else -> "Sync ($pendingSyncCount)"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun BadgeCount(count: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = count.toString(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun RecentCustomerItem(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        SyncStatus.SYNCED -> "Synced" to MaterialTheme.colorScheme.primary
        SyncStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        SyncStatus.SYNCING -> "Syncing" to MaterialTheme.colorScheme.secondary
        else -> syncStatus.name to MaterialTheme.colorScheme.outline
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun PendingTaskItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
            
            TaskPriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun TaskPriorityBadge(priority: com.rudra.isptechniciantool.domain.model.TaskPriority) {
    val color = when (priority) {
        com.rudra.isptechniciantool.domain.model.TaskPriority.URGENT -> MaterialTheme.colorScheme.error
        com.rudra.isptechniciantool.domain.model.TaskPriority.HIGH -> MaterialTheme.colorScheme.tertiary
        com.rudra.isptechniciantool.domain.model.TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondary
        com.rudra.isptechniciantool.domain.model.TaskPriority.LOW -> MaterialTheme.colorScheme.outline
    }
    
    Text(
        text = priority.name,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}
