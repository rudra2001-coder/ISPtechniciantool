package com.rudra.isptechniciantool.ui.screens.tools

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

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
                title = { Text("Ping Tool") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Test Connectivity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Host / IP Address") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("e.g., 192.168.1.1 or google.com") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port (Optional)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("e.g., 80, 443") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
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
                            modifier = Modifier.weight(1f),
                            enabled = !isPinging && host.isNotBlank()
                        ) {
                            if (isPinging) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPinging) "Pinging..." else "Ping")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                host = ""
                                port = ""
                                pingResults = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isPinging
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results
            if (pingResults.isNotEmpty()) {
                Text(
                    text = "Results (${pingResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pingResults.reversed()) { result ->
                        PingResultCard(result = result)
                    }
                }
            } else if (!isPinging && host.isNotBlank()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Enter a host or IP address and tap Ping",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PingResultCard(result: PingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (result.status) {
                PingStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                PingStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                PingStatus.TIMEOUT -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    PingStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                    PingStatus.FAILED -> MaterialTheme.colorScheme.error
                    PingStatus.TIMEOUT -> MaterialTheme.colorScheme.tertiary
                }
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
                    color = MaterialTheme.colorScheme.primary
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
            // Try InetAddress ping first (ICMP-like)
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
                // Try TCP connection
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
            // Try TCP connection as fallback
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
        val latency = System.currentTimeMillis() - startTime
        
        onResult(
            PingResult(
                host = "$host:$port",
                status = PingStatus.SUCCESS,
                message = "TCP connection successful",
                latency = latency.toInt()
            )
        )
        
        socket.close()
    } catch (e: IOException) {
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
    } catch (e: Exception) {
        onResult(
            PingResult(
                host = "$host:$port",
                status = PingStatus.FAILED,
                message = e.message ?: "Unknown error"
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
