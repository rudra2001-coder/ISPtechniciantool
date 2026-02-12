package com.rudra.isptechniciantool.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * MikroTik API Client for connecting to MikroTik routers
 * Uses the MikroTik RouterOS API protocol
 * 
 * Features:
 * - Connection management with timeout
 * - MD5 authentication support
 * - PPP secret management (create, enable, disable, delete)
 * - Custom command execution
 * - Comprehensive error handling
 */
class MikrotikApiClient {

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var isConnected = false

    companion object {
        private const val DEFAULT_API_PORT = 8728
        private const val SOCKET_TIMEOUT = 10000 // 10 seconds
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    /**
     * Connect to a MikroTik router
     * @param ip Router IP address
     * @param port API port (default 8728)
     * @param retryCount Number of retry attempts on failure
     */
    suspend fun connect(
        ip: String, 
        port: Int = DEFAULT_API_PORT,
        retryCount: Int = 1
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(retryCount.coerceAtLeast(1)) { attempt ->
            try {
                socket = Socket()
                socket?.connect(java.net.InetSocketAddress(ip, port), SOCKET_TIMEOUT)
                socket?.soTimeout = SOCKET_TIMEOUT
                
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                
                isConnected = true
                return@withContext Result.success(Unit)
            } catch (e: SocketTimeoutException) {
                lastException = ConnectionException(
                    "Connection timed out to $ip:$port (attempt ${attempt + 1}/$retryCount)", 
                    e
                )
            } catch (e: java.net.ConnectException) {
                lastException = ConnectionException(
                    "Cannot connect to $ip:$port - Is the router reachable? (attempt ${attempt + 1}/$retryCount)", 
                    e
                )
            } catch (e: Exception) {
                lastException = ConnectionException(
                    "Failed to connect to $ip:$port: ${e.message} (attempt ${attempt + 1}/$retryCount)", 
                    e
                )
            }
            
            // Wait before retry
            if (attempt < retryCount - 1) {
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
            }
        }
        
        Result.failure(lastException ?: ConnectionException("Unknown connection error"))
    }

    /**
     * Login to MikroTik router
     * @param username Router username
     * @param password Router password
     */
    suspend fun login(username: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            // Send login command
            val loginCommand = "/login"
            writeSentence(loginCommand)
            
            // Read challenge response
            val response = readSentence()
            
            // Calculate MD5 hash of (challenge + password)
            val challenge = extractChallenge(response)
            
            // If challenge is empty, try direct login (older routers)
            if (challenge.isEmpty()) {
                writeSentence("/login name=$username password=$password")
            } else {
                val responseHash = calculateMd5("$challenge$password")
                val loginCommand2 = "/login name=$username response=$responseHash"
                writeSentence(loginCommand2)
            }
            
            // Read final response
            val finalResponse = readSentence()
            
            // Check for successful login
            if (finalResponse.contains("ret=") || finalResponse.isEmpty()) {
                Result.success(true)
            } else if (finalResponse.contains("message=") || finalResponse.contains("error")) {
                val errorMsg = extractErrorMessage(finalResponse)
                Result.failure(AuthenticationException("Login failed: $errorMsg"))
            } else {
                Result.success(true)
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(AuthenticationException("Login timed out", e))
        } catch (e: Exception) {
            Result.failure(AuthenticationException("Login error: ${e.message}", e))
        }
    }

    /**
     * Create a PPP secret on the router
     * @param username PPP username
     * @param password PPP password
     * @param profile PPP profile
     * @param remoteAddress IP address to assign
     * @param comment Optional comment
     */
    suspend fun createPppSecret(
        username: String,
        password: String,
        profile: String,
        remoteAddress: String? = null,
        comment: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            val command = buildString {
                append("/ppp/secret/add")
                append(" name=${escapeString(username)}")
                append(" password=${escapeString(password)}")
                append(" profile=${escapeString(profile)}")
                remoteAddress?.let { append(" remote-address=${escapeString(it)}") }
                comment?.let { append(" comment=${escapeString(it)}") }
            }
            
            writeSentence(command)
            val response = readSentence()
            
            // Empty response or response with ret= means success
            if (response.isEmpty() || response.contains("ret=")) {
                Result.success(true)
            } else if (response.contains("message=") || response.contains("error")) {
                val errorMsg = extractErrorMessage(response)
                Result.failure(ApiException("Failed to create PPP secret: $errorMsg"))
            } else {
                Result.success(true)
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(ApiException("Command timed out", e))
        } catch (e: Exception) {
            Result.failure(ApiException("Error creating PPP secret: ${e.message}", e))
        }
    }

    /**
     * Get all PPP secrets from the router
     */
    suspend fun getAllSecrets(): Result<List<PppSecret>> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            // Send print command
            writeSentence("/ppp/secret/print")
            
            val secrets = mutableListOf<PppSecret>()
            var response: String
            
            // Read all responses (MikroTik API returns multiple lines)
            while (true) {
                response = readSentence()
                if (response.isEmpty() || response.contains("!done")) {
                    break
                }
                if (response.contains("=.id=")) {
                    secrets.add(parseSecret(response))
                }
            }
            
            Result.success(secrets)
        } catch (e: SocketTimeoutException) {
            Result.failure(ApiException("Query timed out", e))
        } catch (e: Exception) {
            Result.failure(ApiException("Error getting PPP secrets: ${e.message}", e))
        }
    }

    /**
     * Enable a PPP secret
     */
    suspend fun enableSecret(username: String): Result<Boolean> = setSecretStatus(username, true)

    /**
     * Disable a PPP secret
     */
    suspend fun disableSecret(username: String): Result<Boolean> = setSecretStatus(username, false)

    private suspend fun setSecretStatus(username: String, enable: Boolean): Result<Boolean> {
        if (!isConnected) {
            return Result.failure(ConnectionException("Not connected to router"))
        }

        return try {
            // First get the secret ID
            writeSentence("/ppp/secret/print where name=$username")
            val response = readSentence()
            
            val id = extractId(response)
            if (id.isNullOrEmpty()) {
                Result.failure(ApiException("Secret not found: $username"))
            } else {
                // Enable/disable the secret
                val action = if (enable) "enable" else "disable"
                writeSentence("/ppp/secret/$action numbers=$id")
                readSentence() // Read response
                
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Error changing secret status: ${e.message}", e))
        }
    }

    /**
     * Delete a PPP secret
     */
    suspend fun deleteSecret(username: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            // First get the secret ID
            writeSentence("/ppp/secret/print where name=$username")
            val response = readSentence()
            
            val id = extractId(response)
            if (id.isNullOrEmpty()) {
                return@withContext Result.failure(ApiException("Secret not found: $username"))
            }

            // Delete the secret
            writeSentence("/ppp/secret/remove numbers=$id")
            readSentence() // Read response
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiException("Error deleting PPP secret: ${e.message}", e))
        }
    }

    /**
     * Execute a custom command and return the response
     */
    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            writeSentence(command)
            val response = readSentence()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ApiException("Error executing command: ${e.message}", e))
        }
    }

    /**
     * Execute a command and get all responses
     */
    suspend fun executeCommandAll(command: String): Result<List<String>> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            writeSentence(command)
            
            val responses = mutableListOf<String>()
            var response: String
            
            while (true) {
                response = readSentence()
                if (response.isEmpty() || response.contains("!done")) {
                    break
                }
                responses.add(response)
            }
            
            Result.success(responses)
        } catch (e: Exception) {
            Result.failure(ApiException("Error executing command: ${e.message}", e))
        }
    }

    /**
     * Get router resource information
     */
    suspend fun getRouterResources(): Result<RouterResources> = withContext(Dispatchers.IO) {
        if (!isConnected) {
            return@withContext Result.failure(ConnectionException("Not connected to router"))
        }

        try {
            writeSentence("/system/resource/print")
            val response = readSentence()
            
            val version = extractValue(response, "version")
            val boardName = extractValue(response, "board-name")
            val architecture = extractValue(response, "architecture")
            val uptime = extractValue(response, "uptime")
            
            Result.success(
                RouterResources(
                    version = version,
                    boardName = boardName,
                    architecture = architecture,
                    uptime = uptime
                )
            )
        } catch (e: Exception) {
            Result.failure(ApiException("Error getting router resources: ${e.message}", e))
        }
    }

    /**
     * Disconnect from the router
     */
    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignore close errors
        }
        isConnected = false
        writer = null
        reader = null
        socket = null
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean = isConnected

    // Private helper methods

    private fun writeSentence(sentence: String) {
        writer?.write(sentence)
        writer?.newLine()
        writer?.flush()
    }

    private fun readSentence(): String {
        return reader?.readLine() ?: ""
    }

    private fun extractChallenge(response: String): String {
        val retIndex = response.indexOf("=ret=")
        return if (retIndex >= 0) {
            response.substring(retIndex + 5, response.length.coerceAtMost(retIndex + 37))
        } else {
            ""
        }
    }

    private fun extractId(response: String): String? {
        val idIndex = response.indexOf("=.id=")
        return if (idIndex >= 0) {
            val endIndex = response.indexOf(" ", idIndex + 5)
            if (endIndex > 0) {
                response.substring(idIndex + 5, endIndex)
            } else {
                response.substring(idIndex + 5)
            }
        } else {
            null
        }
    }

    private fun extractErrorMessage(response: String): String {
        val msgIndex = response.indexOf("message=")
        return if (msgIndex >= 0) {
            val endIndex = response.indexOf(";", msgIndex + 8)
            if (endIndex > 0) {
                response.substring(msgIndex + 8, endIndex)
            } else {
                response.substring(msgIndex + 8)
            }
        } else {
            response
        }
    }

    private fun extractValue(response: String, key: String): String {
        val searchKey = "$key="
        val index = response.indexOf(searchKey)
        return if (index >= 0) {
            val startIndex = index + searchKey.length
            val endIndex = response.indexOf(" ", startIndex)
            if (endIndex > 0) {
                response.substring(startIndex, endIndex)
            } else {
                response.substring(startIndex)
            }
        } else {
            ""
        }
    }

    private fun parseSecret(response: String): PppSecret {
        var id = ""
        var name = ""
        var password = ""
        var profile = ""
        var remoteAddress = ""
        var disabled = false
        var comment = ""

        val parts = response.split(" ")
        for (part in parts) {
            when {
                part.startsWith("=.id=") -> id = part.substring(5)
                part.startsWith("=name=") -> name = part.substring(6)
                part.startsWith("=password=") -> password = part.substring(10)
                part.startsWith("=profile=") -> profile = part.substring(9)
                part.startsWith("=remote-address=") -> remoteAddress = part.substring(16)
                part.startsWith("=disabled=") -> disabled = part.substring(10) == "true"
                part.startsWith("=comment=") -> comment = part.substring(9)
            }
        }

        return PppSecret(
            id = id,
            name = name,
            password = password,
            profile = profile,
            remoteAddress = remoteAddress,
            disabled = disabled,
            comment = comment
        )
    }

    private fun calculateMd5(input: String): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun escapeString(value: String): String {
        // Escape special characters for MikroTik API
        return value
            .replace("\\", "\\\\")
            .replace(" ", "\\ ")
            .replace("=", "\\=")
    }
}

/**
 * PPP Secret data class
 */
data class PppSecret(
    val id: String,
    val name: String,
    val password: String,
    val profile: String,
    val remoteAddress: String,
    val disabled: Boolean,
    val comment: String = ""
)

/**
 * Router resource information
 */
data class RouterResources(
    val version: String,
    val boardName: String,
    val architecture: String,
    val uptime: String
)

/**
 * Connection exception
 */
class ConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Authentication exception
 */
class AuthenticationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * API exception
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
