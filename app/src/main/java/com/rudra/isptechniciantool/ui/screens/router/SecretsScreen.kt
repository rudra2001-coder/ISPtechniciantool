package com.rudra.isptechniciantool.ui.screens.router

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.isptechniciantool.data.network.PppSecret
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Screen to display and manage PPP secrets from MikroTik router
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecretsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PPP Secrets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadSecrets() },
                        enabled = !uiState.isLoading && !uiState.isProcessing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBlue,
                    titleContentColor = NeutralWhite,
                    navigationIconContentColor = NeutralWhite,
                    actionIconContentColor = NeutralWhite
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadSecrets() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Stats Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total",
                        value = uiState.secrets.size.toString(),
                        icon = Icons.Default.Lock,
                        iconTint = TechBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Enabled",
                        value = uiState.secrets.count { !it.disabled }.toString(),
                        icon = Icons.Default.LockOpen,
                        iconTint = SuccessGreen
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Disabled",
                        value = uiState.secrets.count { it.disabled }.toString(),
                        icon = Icons.Default.Lock,
                        iconTint = ErrorRed
                    )
                }

                // Content
                when {
                    uiState.isLoading && uiState.secrets.isEmpty() -> {
                        LoadingIndicator(
                            modifier = Modifier.fillMaxSize(),
                            message = "Loading secrets from router..."
                        )
                    }
                    uiState.error != null && uiState.secrets.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.Error,
                            title = "Failed to load secrets",
                            message = uiState.error ?: "Unknown error occurred",
                            actionText = "Retry",
                            onAction = { viewModel.loadSecrets() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.secrets.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No PPP secrets found",
                            message = "The router may not have any PPP secrets configured",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        SecretsList(
                            secrets = uiState.secrets,
                            isProcessing = uiState.isProcessing,
                            onEnable = { viewModel.enableSecret(it) },
                            onDisable = { viewModel.disableSecret(it) },
                            onDelete = { viewModel.deleteSecret(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecretsList(
    secrets: List<PppSecret>,
    isProcessing: Boolean,
    onEnable: (PppSecret) -> Unit,
    onDisable: (PppSecret) -> Unit,
    onDelete: (PppSecret) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(
            items = secrets,
            key = { it.id }
        ) { secret ->
            SecretItem(
                secret = secret,
                isProcessing = isProcessing && !secret.disabled,
                onEnable = { onEnable(secret) },
                onDisable = { onDisable(secret) },
                onDelete = { onDelete(secret) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecretItem(
    secret: PppSecret,
    isProcessing: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        ISPDialog(
            title = "Delete Secret",
            message = "Are you sure you want to delete secret \"${secret.name}\"?",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            icon = Icons.Default.Delete
        )
    }
    
    ISPCard(
        modifier = Modifier.fillMaxWidth(),
        cardType = if (secret.disabled) CardType.OUTLINED else CardType.ELEVATED,
        onClick = { /* Navigate to detail */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Icon(
                imageVector = if (secret.disabled) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                tint = if (secret.disabled) ErrorRed else SuccessGreen,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Secret Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = secret.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row {
                    if (secret.remoteAddress.isNotEmpty()) {
                        Text(
                            text = secret.remoteAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Text(
                        text = secret.profile,
                        style = MaterialTheme.typography.bodySmall,
                        color = TechBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (secret.comment.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = secret.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Action Buttons
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = TechBlue
                )
            } else {
                if (secret.disabled) {
                    ISPButton(
                        text = "Enable",
                        onClick = onEnable,
                        buttonType = ButtonType.SUCCESS,
                        height = 32.dp
                    )
                } else {
                    Row {
                        ISPButton(
                            text = "Disable",
                            onClick = onDisable,
                            buttonType = ButtonType.OUTLINE,
                            height = 32.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }
        }
    }
}
