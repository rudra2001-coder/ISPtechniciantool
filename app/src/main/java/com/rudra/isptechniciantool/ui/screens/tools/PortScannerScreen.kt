package com.rudra.isptechniciantool.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortScannerScreen(
    onNavigateBack: () -> Unit
) {
    var targetHost by remember { mutableStateOf("") }
    var startPort by remember { mutableIntStateOf(1) }
    var endPort by remember { mutableIntStateOf(1024) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var scanSpeed by remember { mutableFloatStateOf(5f) }
    var showCommonPortsOnly by remember { mutableStateOf(true) }

    val scanResults = remember { mutableStateListOf<PortScanResult>() }
    val coroutineScope = rememberCoroutineScope()
    val commonPorts = listOf(21, 22, 23, 25, 53, 80, 110, 111, 135, 139, 143, 443, 445, 993, 995, 1723, 3306, 3389, 5432, 5900, 6379, 8080, 8443)

    val tabs = listOf("Scanner", "Results", "Common Ports")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Port Scanner",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Outlined.Dns, contentDescription = null)
                                1 -> Icon(Icons.Outlined.Security, contentDescription = null)
                                2 -> Icon(Icons.Outlined.Public, contentDescription = null)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> ScannerTab(
                    targetHost = targetHost,
                    onTargetHostChange = { targetHost = it },
                    startPort = startPort,
                    onStartPortChange = { startPort = it },
                    endPort = endPort,
                    onEndPortChange = { endPort = it },
                    scanSpeed = scanSpeed,
                    onScanSpeedChange = { scanSpeed = it },
                    showCommonPortsOnly = showCommonPortsOnly,
                    onShowCommonPortsOnlyChange = { showCommonPortsOnly = it },
                    isScanning = isScanning,
                    onStartScan = {
                        scanResults.clear()
                        isScanning = true
                        coroutineScope.launch {
                            simulatePortScan(
                                targetHost,
                                if (showCommonPortsOnly) commonPorts else (startPort..endPort).toList(),
                                scanSpeed,
                                onProgress = { progress -> scanProgress = progress },
                                onResult = { result -> scanResults.add(result) }
                            )
                            isScanning = false
                            scanProgress = 0
                        }
                    },
                    onStopScan = {
                        isScanning = false
                        scanProgress = 0
                    },
                    scanProgress = scanProgress
                )

                1 -> ResultsTab(
                    scanResults = scanResults,
                    targetHost = targetHost,
                    onClearResults = { scanResults.clear() }
                )

                2 -> CommonPortsTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerTab(
    targetHost: String,
    onTargetHostChange: (String) -> Unit,
    startPort: Int,
    onStartPortChange: (Int) -> Unit,
    endPort: Int,
    onEndPortChange: (Int) -> Unit,
    scanSpeed: Float,
    onScanSpeedChange: (Float) -> Unit,
    showCommonPortsOnly: Boolean,
    onShowCommonPortsOnlyChange: (Boolean) -> Unit,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    scanProgress: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Target Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = targetHost,
                        onValueChange = onTargetHostChange,
                        label = { Text("Hostname or IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g., 192.168.1.1 or scanme.nmap.org") },
                        isError = targetHost.isNotBlank() && !isValidHost(targetHost),
                        supportingText = if (targetHost.isNotBlank() && !isValidHost(targetHost)) {
                            { Text("Invalid host format", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Common Ports Only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = showCommonPortsOnly,
                            onCheckedChange = onShowCommonPortsOnlyChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (!showCommonPortsOnly) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startPort.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let { port ->
                                        onStartPortChange(port.coerceIn(1, 65535))
                                    }
                                },
                                label = { Text("From") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = startPort > endPort
                            )

                            OutlinedTextField(
                                value = endPort.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let { port ->
                                        onEndPortChange(port.coerceIn(1, 65535))
                                    }
                                },
                                label = { Text("To") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = endPort < startPort
                            )
                        }

                        if (startPort > endPort) {
                            Text(
                                text = "Start port must be less than end port",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scan Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scan Speed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when {
                                scanSpeed < 3f -> "Slow (Stealth)"
                                scanSpeed < 7f -> "Normal"
                                else -> "Fast (Aggressive)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = scanSpeed,
                        onValueChange = onScanSpeedChange,
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SpeedLabel(text = "Stealth", isSelected = scanSpeed < 3f)
                        SpeedLabel(text = "Normal", isSelected = scanSpeed in 3f..7f)
                        SpeedLabel(text = "Aggressive", isSelected = scanSpeed > 7f)
                    }
                }
            }
        }

        item {
            if (isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Scanning ports... $scanProgress%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = if (isScanning) onStopScan else onStartScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ),
                enabled = targetHost.isNotBlank() &&
                        (showCommonPortsOnly || (startPort <= endPort))
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "Stop Scan" else "Start Scan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ResultsTab(
    scanResults: List<PortScanResult>,
    targetHost: String,
    onClearResults: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scan Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (scanResults.isNotEmpty()) {
                Button(
                    onClick = onClearResults,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scanResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (targetHost.isBlank())
                            "Configure and start a scan"
                        else "No open ports found on $targetHost",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "Found ${scanResults.size} open port${if (scanResults.size > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scanResults) { result ->
                    PortResultCard(result)
                }
            }
        }
    }
}

@Composable
fun PortResultCard(
    result: PortScanResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Port number
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = result.port.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/tcp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = result.service,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Additional info
            if (result.isCommon) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Common",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (result.isSecure) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Secure",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun CommonPortsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Common TCP Ports Reference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(commonPortsInfo) { portInfo ->
            CommonPortInfoCard(portInfo)
        }
    }
}

@Composable
fun CommonPortInfoCard(
    portInfo: CommonPortInfo
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = portInfo.port.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = portInfo.service,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = portInfo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SpeedLabel(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}

// Data classes
data class PortScanResult(
    val port: Int,
    val service: String,
    val isOpen: Boolean = true,
    val isCommon: Boolean = false,
    val isSecure: Boolean = false
)

data class CommonPortInfo(
    val port: Int,
    val service: String,
    val description: String
)

// Common ports reference
val commonPortsInfo = listOf(
    CommonPortInfo(21, "FTP", "File Transfer Protocol - unencrypted file transfers"),
    CommonPortInfo(22, "SSH", "Secure Shell - secure remote administration"),
    CommonPortInfo(23, "Telnet", "Telnet - unencrypted remote administration"),
    CommonPortInfo(25, "SMTP", "Simple Mail Transfer Protocol - email routing"),
    CommonPortInfo(53, "DNS", "Domain Name System - domain resolution"),
    CommonPortInfo(80, "HTTP", "Hypertext Transfer Protocol - web traffic"),
    CommonPortInfo(110, "POP3", "Post Office Protocol v3 - email retrieval"),
    CommonPortInfo(143, "IMAP", "Internet Message Access Protocol - email retrieval"),
    CommonPortInfo(443, "HTTPS", "HTTP Secure - encrypted web traffic"),
    CommonPortInfo(445, "SMB", "Server Message Block - file sharing"),
    CommonPortInfo(993, "IMAPS", "IMAP over SSL - encrypted email"),
    CommonPortInfo(995, "POP3S", "POP3 over SSL - encrypted email"),
    CommonPortInfo(3306, "MySQL", "MySQL database"),
    CommonPortInfo(3389, "RDP", "Remote Desktop Protocol"),
    CommonPortInfo(5432, "PostgreSQL", "PostgreSQL database"),
    CommonPortInfo(8080, "HTTP-Alt", "Alternative HTTP port"),
    CommonPortInfo(8443, "HTTPS-Alt", "Alternative HTTPS port")
)

// Helper functions
private fun isValidHost(host: String): Boolean {
    // Basic validation for IP address or hostname
    val ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    val hostnamePattern = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]?(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]?)*$"
    return host.matches(Regex(ipPattern)) || host.matches(Regex(hostnamePattern))
}

// Simulated port scan function (replace with actual implementation)
private suspend fun simulatePortScan(
    host: String,
    ports: List<Int>,
    speed: Float,
    onProgress: (Int) -> Unit,
    onResult: (PortScanResult) -> Unit
) {
    val totalPorts = ports.size
    ports.forEachIndexed { index, port ->
        if (totalPorts > 0) {
            val progress = ((index + 1) * 100) / totalPorts
            onProgress(progress)
        }

        // Simulate port check
        delay((1000 / speed).toLong())

        // Mock some open ports for demonstration
        if (port in listOf(22, 80, 443, 3389, 8080)) {
            onResult(
                PortScanResult(
                    port = port,
                    service = when (port) {
                        22 -> "SSH"
                        80 -> "HTTP"
                        443 -> "HTTPS"
                        3389 -> "RDP"
                        8080 -> "HTTP-Proxy"
                        else -> "Unknown"
                    },
                    isCommon = port in listOf(22, 80, 443, 3389, 8080, 21, 25, 53, 3306),
                    isSecure = port in listOf(22, 443, 993, 995, 8443)
                )
            )
        }
    }
}