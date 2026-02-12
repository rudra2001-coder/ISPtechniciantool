package com.rudra.isptechniciantool.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import com.rudra.isptechniciantool.ui.components.*
import com.rudra.isptechniciantool.ui.components.CardType
import com.rudra.isptechniciantool.ui.components.ButtonType
import com.rudra.isptechniciantool.ui.theme.*

/**
 * Ping tool screen for network connectivity testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PingToolScreen(
    onNavigateBack: () -> Unit
) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var isPinging by remember { mutableStateOf(false) }
    var pingResults by remember { mutableStateOf<List<PingResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ping Tool", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input Card
            ISPCard(
                modifier = Modifier.fillMaxWidth(),
                cardType = CardType.ELEVATED
            ) {
                Text(
                    text = "Test Connectivity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host / IP Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 192.168.1.1 or google.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = TechBlue
                        )
                    },
                    trailingIcon = {
                        if (host.isNotEmpty()) {
                            IconButton(onClick = { host = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 80, 443") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = TechBlue
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ISPButton(
                        text = if (isPinging) "Pinging..." else "Ping",
                        onClick = {
                            if (host.isNotBlank()) {
                                isPinging = true
                                pingResults = emptyList()
                                scope.launch {
                                    performPing(host, port.toIntOrNull()) { result ->
                                        pingResults = pingResults + result
                                    }
                                    isPinging = false
                                }
                            }
                        },
                        icon = if (isPinging) null else Icons.Default.PlayArrow,
                        loading = isPinging,
                        enabled = !isPinging && host.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    
                    ISPButton(
                        text = "Clear",
                        onClick = {
                            host = ""
                            port = ""
                            pingResults = emptyList()
                        },
                        icon = Icons.Default.Clear,
                        buttonType = ButtonType.OUTLINE,
                        enabled = !isPinging,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results
            when {
                pingResults.isNotEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Results (${pingResults.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val successRate = (pingResults.count { it.status == PingStatus.SUCCESS } * 100 / 
                                          if (pingResults.isNotEmpty()) pingResults.size else 1)
                        StatusBadge(
                            text = "$successRate% success",
                            color = if (successRate >= 50) SuccessGreen else ErrorRed
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pingResults.reversed()) { result ->
                            PingResultCard(result = result)
                        }
                    }
                }
                !isPinging && host.isNotBlank() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Ready to Test",
                        message = "Enter a host or IP address and tap Ping",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                !isPinging && host.isBlank() -> {
                    EmptyState(
                        icon = Icons.Default.Wifi,
                        title = "Network Diagnostics",
                        message = "Test host connectivity and port availability",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PingResultCard(result: PingResult) {
    ISPCard(
        modifier = Modifier.fillMaxWidth(),
        cardType = CardType.DEFAULT,
        backgroundColor = when (result.status) {
            PingStatus.SUCCESS -> SuccessGreen.copy(alpha = 0.05f)
            PingStatus.FAILED -> ErrorRed.copy(alpha = 0.05f)
            PingStatus.TIMEOUT -> WarningOrange.copy(alpha = 0.05f)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (result.status) {
                    PingStatus.SUCCESS -> Icons.Default.CheckCircle
                    PingStatus.FAILED -> Icons.Default.Error
                    PingStatus.TIMEOUT -> Icons.Default.Timer
                },
                contentDescription = null,
                tint = when (result.status) {
                    PingStatus.SUCCESS -> SuccessGreen
                    PingStatus.FAILED -> ErrorRed
                    PingStatus.TIMEOUT -> WarningOrange
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.host,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (result.latency != null) {
                Text(
                    text = "${result.latency}ms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (result.status) {
                        PingStatus.SUCCESS -> SuccessGreen
                        PingStatus.FAILED -> ErrorRed
                        PingStatus.TIMEOUT -> WarningOrange
                    }
                )
            }
        }
    }
}

private suspend fun performPing(
    host: String,
    port: Int?,
    onResult: (PingResult) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(host)
            val startTime = System.currentTimeMillis()
            val reachable = address.isReachable(3000)
            val latency = System.currentTimeMillis() - startTime
            
            if (reachable) {
                onResult(
                    PingResult(
                        host = host,
                        status = PingStatus.SUCCESS,
                        message = "Host reachable",
                        latency = latency.toInt()
                    )
                )
            } else {
                if (port != null) {
                    tryTcpConnect(host, port, onResult)
                } else {
                    onResult(
                        PingResult(
                            host = host,
                            status = PingStatus.FAILED,
                            message = "Host unreachable (ICMP blocked)"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            if (port != null) {
                tryTcpConnect(host, port, onResult)
            } else {
                onResult(
                    PingResult(
                        host = host,
                        status = PingStatus.FAILED,
                        message = e.message ?: "Unknown error"
                    )
                )
            }
        }
    }
}

private fun tryTcpConnect(
    host: String,
    port: Int,
    onResult: (PingResult) -> Unit
) {
    try {
        val socket = Socket()
        val startTime = System.currentTimeMillis()
        
        socket.connect(InetSocketAddress(host, port), 3000)
        val latency = (System.currentTimeMillis() - startTime).toInt()
        
        onResult(
            PingResult(
                host = "$host:$port",
                status = PingStatus.SUCCESS,
                message = "TCP connection successful",
                latency = latency
            )
        )
        
        socket.close()
    } catch (e: Exception) {
        onResult(
            PingResult(
                host = "$host:$port",
                status = if (e.message?.contains("timed out") == true) {
                    PingStatus.TIMEOUT
                } else {
                    PingStatus.FAILED
                },
                message = e.message ?: "Connection failed"
            )
        )
    }
}

data class PingResult(
    val host: String,
    val status: PingStatus,
    val message: String,
    val latency: Int? = null
)

enum class PingStatus {
    SUCCESS,
    FAILED,
    TIMEOUT
}
