package com.rudra.isptechniciantool.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Utility class for network connectivity checks.
 * Provides ping (ICMP) and TCP port connectivity testing.
 */
object PingUtil {

    // Default timeout for ping operations in milliseconds
    const val DEFAULT_PING_TIMEOUT = 2000

    // Default timeout for TCP connection attempts in milliseconds
    const val DEFAULT_TCP_TIMEOUT = 2000

    // Common TCP ports for network device services
    object CommonPorts {
        const val SSH = 22
        const val TELNET = 23
        const val HTTP = 80
        const val HTTPS = 443
        const val SNMP = 161
        const val MIKROTIK_API = 8728
        const val MIKROTIK_API_SSL = 8729
    }

    /**
     * Result of a ping operation.
     */
    data class PingResult(
        val success: Boolean,
        val latency: Long? = null, // Latency in milliseconds
        val errorMessage: String? = null
    )

    /**
     * Result of a TCP port check.
     */
    data class TcpCheckResult(
        val port: Int,
        val reachable: Boolean,
        val latency: Long? = null, // Latency in milliseconds
        val errorMessage: String? = null
    )

    /**
     * Check if a host is reachable using ICMP ping.
     * Uses Java's InetAddress.isReachable() which may use ICMP or TCP echo.
     *
     * @param host The IP address or hostname to ping
     * @param timeout Timeout in milliseconds for the ping operation
     * @return PingResult indicating success/failure and latency
     */
    suspend fun ping(host: String, timeout: Int = DEFAULT_PING_TIMEOUT): PingResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                if (host.isBlank()) {
                    return@withContext PingResult(
                        success = false,
                        errorMessage = "Host address is empty"
                    )
                }

                val address = InetAddress.getByName(host)
                val reachable = address.isReachable(timeout)
                val latency = System.currentTimeMillis() - startTime

                if (reachable) {
                    PingResult(success = true, latency = latency)
                } else {
                    PingResult(
                        success = false,
                        latency = latency,
                        errorMessage = "Host is not reachable"
                    )
                }
            } catch (e: Exception) {
                val latency = try {
                    System.currentTimeMillis() - startTime
                } catch (_: Exception) { null }

                PingResult(
                    success = false,
                    latency = latency,
                    errorMessage = when (e) {
                        is java.net.UnknownHostException -> "Unknown host: ${e.message}"
                        is IOException -> "Network error: ${e.message}"
                        is SecurityException -> "Permission denied: ${e.message}"
                        else -> "Error: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Check if a specific TCP port is open on a host.
     *
     * @param host The IP address or hostname to check
     * @param port The TCP port to check
     * @param timeout Timeout in milliseconds for the connection attempt
     * @return TcpCheckResult indicating if the port is reachable
     */
    suspend fun checkTcpPort(
        host: String,
        port: Int,
        timeout: Int = DEFAULT_TCP_TIMEOUT
    ): TcpCheckResult {
        return withContext(Dispatchers.IO) {
            if (host.isBlank()) {
                return@withContext TcpCheckResult(
                    port = port,
                    reachable = false,
                    errorMessage = "Host address is empty"
                )
            }

            if (port < 1 || port > 65535) {
                return@withContext TcpCheckResult(
                    port = port,
                    reachable = false,
                    errorMessage = "Invalid port number"
                )
            }

            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), timeout)
                socket.close()
                val latency = System.currentTimeMillis() - startTime

                TcpCheckResult(
                    port = port,
                    reachable = true,
                    latency = latency
                )
            } catch (e: Exception) {
                TcpCheckResult(
                    port = port,
                    reachable = false,
                    latency = null,
                    errorMessage = when (e) {
                        is java.net.ConnectException -> "Connection refused"
                        is java.net.SocketTimeoutException -> "Connection timed out"
                        is java.net.UnknownHostException -> "Unknown host: ${e.message}"
                        is IOException -> "Network error: ${e.message}"
                        else -> "Error: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Check multiple common ports on a host.
     *
     * @param host The IP address or hostname to check
     * @param ports List of ports to check (defaults to common network service ports)
     * @param timeout Timeout in milliseconds for each connection attempt
     * @return List of TcpCheckResult for each port checked
     */
    suspend fun checkCommonPorts(
        host: String,
        ports: List<Int> = listOf(
            CommonPorts.SSH,
            CommonPorts.HTTP,
            CommonPorts.HTTPS
        ),
        timeout: Int = DEFAULT_TCP_TIMEOUT
    ): List<TcpCheckResult> {
        return ports.map { port ->
            checkTcpPort(host, port, timeout)
        }
    }

    /**
     * Perform a comprehensive connectivity check on a device.
     * First attempts ICMP ping, then checks common TCP ports.
     *
     * @param ipAddress The IP address of the device
     * @param pingTimeout Timeout for ping operation
     * @param tcpTimeout Timeout for TCP connection attempts
     * @return A comprehensive check result
     */
    suspend fun checkDeviceConnectivity(
        ipAddress: String,
        pingTimeout: Int = DEFAULT_PING_TIMEOUT,
        tcpTimeout: Int = DEFAULT_TCP_TIMEOUT
    ): DeviceConnectivityResult {
        if (ipAddress.isBlank()) {
            return DeviceConnectivityResult(
                ipAddress = ipAddress,
                pingSuccess = false,
                tcpPortsChecked = emptyList(),
                overallStatus = ConnectivityStatus.UNREACHABLE,
                errorMessage = "IP address is empty"
            )
        }

        // First, try ping
        val pingResult = ping(ipAddress, pingTimeout)

        // If ping succeeds, device is online
        if (pingResult.success) {
            return DeviceConnectivityResult(
                ipAddress = ipAddress,
                pingSuccess = true,
                pingLatency = pingResult.latency,
                tcpPortsChecked = emptyList(),
                overallStatus = ConnectivityStatus.ONLINE
            )
        }

        // If ping fails, try checking common TCP ports
        val tcpResults = checkCommonPorts(ipAddress, timeout = tcpTimeout)
        val reachablePorts = tcpResults.filter { it.reachable }

        val overallStatus = when {
            reachablePorts.isNotEmpty() -> ConnectivityStatus.DEGRADED
            else -> ConnectivityStatus.OFFLINE
        }

        return DeviceConnectivityResult(
            ipAddress = ipAddress,
            pingSuccess = false,
            pingLatency = pingResult.latency,
            tcpPortsChecked = tcpResults,
            overallStatus = overallStatus,
            errorMessage = if (reachablePorts.isEmpty()) pingResult.errorMessage else null
        )
    }

    /**
     * Comprehensive device connectivity result.
     */
    data class DeviceConnectivityResult(
        val ipAddress: String,
        val pingSuccess: Boolean,
        val pingLatency: Long? = null,
        val tcpPortsChecked: List<TcpCheckResult>,
        val overallStatus: ConnectivityStatus,
        val errorMessage: String? = null
    ) {
        /**
         * Get the best latency measurement (ping or TCP).
         */
        val bestLatency: Long?
            get() = pingLatency ?: tcpPortsChecked.firstOrNull { it.latency != null }?.latency

        /**
         * Get list of successfully reachable TCP ports.
         */
        val reachablePorts: List<Int>
            get() = tcpPortsChecked.filter { it.reachable }.map { it.port }
    }

    /**
     * Overall connectivity status for a device.
     */
    enum class ConnectivityStatus {
        ONLINE,      // Ping successful
        DEGRADED,    // Ping failed but some TCP ports reachable
        OFFLINE,     // No connectivity
        UNREACHABLE  // Could not determine status
    }
}
