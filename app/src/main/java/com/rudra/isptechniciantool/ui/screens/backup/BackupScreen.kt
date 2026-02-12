package com.rudra.isptechniciantool.ui.screens.backup

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Backup and Export screen for data management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var isProcessing by remember { mutableStateOf(false) }
    var processingText by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isProcessing = true
                processingText = "Restoring data..."
                val success = viewModel.restoreFromUri(context, it)
                isProcessing = false
                if (success) {
                    snackbarHostState.showSnackbar("Data restored successfully!")
                } else {
                    snackbarHostState.showSnackbar("Failed to restore data")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Export", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Backup Section
                item {
                    SectionHeader(title = "Backup Options")
                }
                
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = CardType.ELEVATED
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Full Backup",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Export all data to JSON file",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ISPButton(
                            text = "Export & Share Backup",
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    processingText = "Creating backup..."
                                    val file = viewModel.exportToJson(context)
                                    isProcessing = false
                                    
                                    if (file != null) {
                                        val uri = viewModel.getFileUri(context, file)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to create backup")
                                    }
                                }
                            },
                            icon = Icons.Default.CloudUpload,
                            loading = isProcessing,
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Restore Section
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = CardType.OUTLINED
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Restore Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Import from backup file",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ISPButton(
                            text = "Import from File",
                            onClick = {
                                filePickerLauncher.launch(arrayOf("application/json"))
                            },
                            icon = Icons.Default.FileUpload,
                            buttonType = ButtonType.SECONDARY,
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Export CSV Section
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = CardType.DEFAULT
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Export Customers (CSV)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "For spreadsheets and reports",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ISPButton(
                            text = "Export CSV",
                            onClick = {
                                scope.launch {
                                    val file = viewModel.exportCustomersToCsv(context)
                                    if (file != null) {
                                        val uri = viewModel.getFileUri(context, file)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share CSV"))
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to export CSV")
                                    }
                                }
                            },
                            icon = Icons.Default.TableChart,
                            buttonType = ButtonType.OUTLINE,
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Tips Section
                item {
                    ISPCard(
                        modifier = Modifier.fillMaxWidth(),
                        cardType = CardType.DEFAULT
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Backup Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column {
                            TipRow(text = "Regular backups protect your data")
                            TipRow(text = "Export CSV for office reporting")
                            TipRow(text = "Share backup files securely")
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Loading overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = TechBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = processingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
