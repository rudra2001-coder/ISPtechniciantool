package com.rudra.isptechniciantool.ui.screens.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.util.PermissionUtils

/**
 * Customer detail screen showing all customer information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CustomerDetailEvent.Deleted -> {
                    Toast.makeText(context, "Customer deleted", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is CustomerDetailEvent.SyncRetried -> {
                    Toast.makeText(context, "Sync retried", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Customer") },
            text = { Text("Are you sure you want to delete this customer? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.customer?.let { customer ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${customer.phone}")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Phone, contentDescription = "Call")
                        }
                    }
                    IconButton(onClick = { /* Navigate to edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            uiState.customer?.let { customer ->
                CustomerDetailContent(
                    customer = customer,
                    modifier = Modifier.padding(paddingValues),
                    onRetrySync = { viewModel.retrySync() }
                )
            }
        }
    }
}

@Composable
private fun CustomerDetailContent(
    customer: Customer,
    modifier: Modifier = Modifier,
    onRetrySync: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with Sync Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SyncStatusBadge(syncStatus = customer.syncStatus, showDetails = true)
                    if (customer.syncStatus == SyncStatus.FAILED) {
                        OutlinedButton(onClick = onRetrySync) {
                            Text("Retry Sync")
                        }
                    }
                }
            }
        }
        
        // Contact Information
        DetailSection(title = "Contact Information") {
            DetailRow(label = "Phone", value = customer.phone)
            if (customer.phoneSecondary != null) {
                DetailRow(label = "Alt Phone", value = customer.phoneSecondary)
            }
            if (customer.email != null) {
                DetailRow(label = "Email", value = customer.email)
            }
        }
        
        // Address Information
        DetailSection(title = "Service Address") {
            DetailRow(label = "Address", value = customer.address)
            if (customer.addressNotes != null && customer.addressNotes.isNotEmpty()) {
                DetailRow(label = "Notes", value = customer.addressNotes)
            }
        }
        
        // PPP Credentials
        DetailSection(title = "PPP Credentials") {
            DetailRow(label = "Username", value = customer.username)
            DetailRow(label = "Password", value = customer.password)
        }
        
        // Network Settings
        DetailSection(title = "Network Settings") {
            DetailRow(label = "IP Address", value = customer.ipAddress ?: "Dynamic")
            DetailRow(label = "Package", value = customer.packageName ?: "Not assigned")
        }
        
        // Status & Notes
        DetailSection(title = "Additional Information") {
            DetailRow(label = "Status", value = customer.customerStatus.name)
            if (customer.notes != null && customer.notes.isNotEmpty()) {
                DetailRow(label = "Notes", value = customer.notes)
            }
            DetailRow(
                label = "Last Modified",
                value = customer.modifiedAt.toString()
            )
            if (customer.syncedAt != null) {
                DetailRow(
                    label = "Last Synced",
                    value = customer.syncedAt.toString()
                )
            }
        }
        
        if (customer.syncStatus == SyncStatus.FAILED && customer.syncError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sync Error",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = customer.syncError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "-" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SyncStatusBadge(
    syncStatus: SyncStatus,
    showDetails: Boolean = false
) {
    val (text, color) = when (syncStatus) {
        SyncStatus.SYNCED -> "Synced" to MaterialTheme.colorScheme.primary
        SyncStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        SyncStatus.SYNCING -> "Syncing..." to MaterialTheme.colorScheme.secondary
        SyncStatus.CONFLICT -> "Conflict" to MaterialTheme.colorScheme.error
        SyncStatus.DELETING -> "Deleting" to MaterialTheme.colorScheme.outline
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
