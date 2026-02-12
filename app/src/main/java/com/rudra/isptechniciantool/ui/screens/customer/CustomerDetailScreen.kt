package com.rudra.isptechniciantool.ui.screens.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.components.CardType
import com.rudra.isptechniciantool.ui.components.ButtonType
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Customer detail screen showing all customer information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
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
        ISPDialog(
            title = "Delete Customer",
            message = "Are you sure you want to delete ${uiState.customer?.name}? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteCustomer()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            icon = Icons.Default.Delete
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
                        IconButton(onClick = { onNavigateToEdit(customerId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBlue,
                    titleContentColor = NeutralWhite,
                    navigationIconContentColor = NeutralWhite,
                    actionIconContentColor = NeutralWhite
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading customer..."
                )
            }
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    message = uiState.error ?: "Unknown error occurred",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.customer != null -> {
                CustomerDetailContent(
                    customer = uiState.customer!!,
                    modifier = Modifier.padding(paddingValues),
                    onRetrySync = { viewModel.retrySync() },
                    onCallCustomer = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomerDetailContent(
    customer: Customer,
    modifier: Modifier = Modifier,
    onRetrySync: () -> Unit,
    onCallCustomer: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with Customer Info
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.ELEVATED
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralWhite
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncStatusBadge(syncStatus = customer.syncStatus)
                        if (customer.syncStatus == SyncStatus.FAILED) {
                            ISPButton(
                                text = "Retry",
                                onClick = onRetrySync,
                                buttonType = ButtonType.OUTLINE,
                                height = 28.dp
                            )
                        }
                    }
                }
            }
        }
        
        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Phone,
                text = "Call",
                onClick = { onCallCustomer(customer.phone) }
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Message,
                text = "SMS",
                onClick = { /* TODO: Open SMS */ }
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Wifi,
                text = "Test IP",
                enabled = customer.ipAddress != null,
                onClick = { /* TODO: Test IP */ }
            )
        }
        
        // Contact Information
        SectionHeader(title = "Contact Information")
        
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.DEFAULT
        ) {
            InfoRow(
                label = "Phone",
                value = customer.phone,
                icon = Icons.Default.Phone,
                badge = true,
                badgeColor = SuccessGreen,
                onClick = { onCallCustomer(customer.phone) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (customer.phoneSecondary != null) {
                InfoRow(
                    label = "Alt Phone",
                    value = customer.phoneSecondary,
                    icon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (customer.email != null) {
                InfoRow(
                    label = "Email",
                    value = customer.email,
                    icon = Icons.Default.Email
                )
            }
        }
        
        // Address Information
        SectionHeader(title = "Service Address")
        
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.DEFAULT
        ) {
            InfoRow(
                label = "Address",
                value = customer.address,
                icon = Icons.Default.LocationOn
            )
            if (customer.addressNotes != null && customer.addressNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    label = "Notes",
                    value = customer.addressNotes,
                    icon = Icons.Default.Notes
                )
            }
        }
        
        // PPP Credentials
        SectionHeader(title = "PPP Credentials")
        
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.OUTLINED
        ) {
            InfoRow(
                label = "Username",
                value = customer.username,
                icon = Icons.Default.AccountCircle
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                label = "Password",
                value = customer.password,
                icon = Icons.Default.Lock
            )
        }
        
        // Network Settings
        SectionHeader(title = "Network Settings")
        
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.DEFAULT
        ) {
            InfoRow(
                label = "IP Address",
                value = customer.ipAddress ?: "Dynamic",
                icon = Icons.Default.SettingsEthernet
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                label = "Package",
                value = customer.packageName ?: "Not assigned",
                icon = Icons.Default.Speed
            )
        }
        
        // Additional Information
        SectionHeader(title = "Additional Information")
        
        ISPCard(
            modifier = Modifier.fillMaxWidth(),
            cardType = CardType.DEFAULT
        ) {
            InfoRow(
                label = "Status",
                value = customer.customerStatus.name,
                icon = Icons.Default.Info
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                label = "Last Modified",
                value = customer.modifiedAt.toString(),
                icon = Icons.Default.EditCalendar
            )
            if (customer.syncedAt != null) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    label = "Last Synced",
                    value = customer.syncedAt.toString(),
                    icon = Icons.Default.Sync
                )
            }
            if (customer.notes != null && customer.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    label = "Notes",
                    value = customer.notes,
                    icon = Icons.Default.Notes
                )
            }
        }
        
        // Sync Error Display
        if (customer.syncStatus == SyncStatus.FAILED && customer.syncError != null) {
            ISPCard(
                modifier = Modifier.fillMaxWidth(),
                cardType = CardType.DEFAULT
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sync Error",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                        Text(
                            text = customer.syncError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (enabled) TechBlue else NeutralGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer 
                       else NeutralGray
            )
        }
    }
}

@Composable
private fun SyncStatusBadge(syncStatus: SyncStatus) {
    val (text, color) = when (syncStatus) {
        SyncStatus.SYNCED -> "Synced" to SuccessGreen
        SyncStatus.PENDING -> "Pending" to WarningOrange
        SyncStatus.FAILED -> "Failed" to ErrorRed
        SyncStatus.SYNCING -> "Syncing..." to TechBlue
        SyncStatus.CONFLICT -> "Conflict" to ErrorRed
        SyncStatus.DELETING -> "Deleting" to NeutralGray
    }
    
    StatusBadge(text = text, color = color)
}
