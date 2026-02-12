package com.rudra.isptechniciantool.domain.usecase

import com.rudra.isptechniciantool.data.network.ApiException
import com.rudra.isptechniciantool.data.network.AuthenticationException
import com.rudra.isptechniciantool.data.network.ConnectionException
import com.rudra.isptechniciantool.data.network.MikrotikApiClient
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.Router
import com.rudra.isptechniciantool.domain.model.SyncLog
import com.rudra.isptechniciantool.domain.model.SyncOperationType
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.model.SyncOutcome
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.SyncLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Sync Engine for synchronizing customers with MikroTik router
 * 
 * Features:
 * - Batch sync of pending customers
 * - Individual customer sync
 * - Retry logic for failed syncs
 * - Comprehensive error handling
 * - Sync logging for audit trail
 */
class SyncEngine(
    private val routerRepository: RouterRepository,
    private val customerRepository: CustomerRepository,
    private val syncLogRepository: SyncLogRepository
) {
    private val apiClient = MikrotikApiClient()
    
    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * Sync all pending customers to the router
     * @param retryFailed If true, will retry customers that previously failed
     */
    suspend fun syncAllPendingCustomers(retryFailed: Boolean = false): SyncResult = withContext(Dispatchers.IO) {
        val router = routerRepository.getDefaultEnabledRouter()
            ?: return@withContext SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = "No router configured. Please configure router settings first."
            )

        val log = SyncLog(
            startTime = LocalDateTime.now(),
            totalAttempted = 0,
            successCount = 0,
            failedCount = 0,
            status = SyncStatus.SYNCING,
            operationType = SyncOperationType.BATCH_SYNC
        )
        val logId = syncLogRepository.createSyncLog(log)

        var successCount = 0
        var failedCount = 0

        try {
            // Connect to router
            val connectResult = apiClient.connect(router.host, router.port)
            
            if (connectResult.isFailure) {
                val error = connectResult.exceptionOrNull()?.message ?: "Connection failed"
                return@withContext createFailedSyncResult(
                    logId,
                    "Failed to connect to router: $error",
                    SyncOutcome.NETWORK_ERROR
                )
            }

            // Login to router
            val loginResult = apiClient.login(router.username, router.password)
            
            if (loginResult.isFailure) {
                apiClient.disconnect()
                val error = loginResult.exceptionOrNull()?.message ?: "Authentication failed"
                return@withContext createFailedSyncResult(
                    logId,
                    "Login failed: $error",
                    SyncOutcome.AUTH_ERROR
                )
            }

            // Get pending customers
            val pendingCustomers = customerRepository.getCustomersByStatus(CustomerStatus.PENDING)
            
            // Get failed customers if retrying
            val failedCustomers = if (retryFailed) {
                customerRepository.getCustomersByStatus(CustomerStatus.INACTIVE)
            } else {
                emptyList()
            }
            
            val allCustomersToSync = pendingCustomers + failedCustomers

            if (allCustomersToSync.isEmpty()) {
                apiClient.disconnect()
                return@withContext SyncResult(
                    success = true,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = null
                )
            }

            for (customer in allCustomersToSync) {
                try {
                    val syncResult = syncSingleCustomer(customer, router)
                    
                    if (syncResult.success) {
                        successCount++
                    } else {
                        failedCount++
                    }
                } catch (e: Exception) {
                    // Update customer status to INACTIVE
                    val failedCustomer = customer.copy(
                        customerStatus = CustomerStatus.INACTIVE,
                        syncError = e.message ?: "Unknown error",
                        syncRetryCount = customer.syncRetryCount + 1
                    )
                    customerRepository.updateCustomer(failedCustomer)
                    failedCount++
                }
            }

            // Disconnect from router
            apiClient.disconnect()

            // Update sync log
            val endTime = LocalDateTime.now()
            val finalStatus = when {
                failedCount == 0 -> SyncStatus.SYNCED
                successCount == 0 -> SyncStatus.FAILED
                else -> SyncStatus.FAILED
            }
            
            val errorMessage = if (failedCount > 0) {
                "$failedCount customers failed to sync"
            } else {
                null
            }
            
            syncLogRepository.updateSyncLog(
                log.copy(
                    id = logId,
                    endTime = endTime,
                    totalAttempted = successCount + failedCount,
                    successCount = successCount,
                    failedCount = failedCount,
                    status = finalStatus,
                    errorMessage = errorMessage,
                    outcome = if (failedCount == 0) SyncOutcome.SUCCESS else SyncOutcome.FAILURE
                )
            )

            SyncResult(
                success = successCount > 0,
                totalSynced = successCount + failedCount,
                successCount = successCount,
                failedCount = failedCount,
                errorMessage = errorMessage
            )
        } catch (e: Exception) {
            apiClient.disconnect()
            createFailedSyncResult(
                logId,
                "Sync error: ${e.message}",
                SyncOutcome.FAILURE
            )
        }
    }

    /**
     * Sync a single customer to the router with retry logic
     */
    private suspend fun syncSingleCustomer(customer: Customer, router: Router): SyncResult {
        var lastError: Exception? = null
        
        for (attempt in 1..MAX_RETRIES) {
            try {
                // Create PPP secret
                val createResult = apiClient.createPppSecret(
                    username = customer.username,
                    password = customer.password,
                    profile = customer.packageName ?: "default",
                    remoteAddress = customer.ipAddress ?: ""
                )

                if (createResult.isSuccess) {
                    // Update customer sync status to SYNCED
                    val updatedCustomer = customer.copy(
                        syncStatus = SyncStatus.SYNCED,
                        syncedAt = LocalDateTime.now(),
                        syncError = null,
                        syncRetryCount = 0
                    )
                    customerRepository.updateCustomer(updatedCustomer)
                    
                    return SyncResult(
                        success = true,
                        totalSynced = 1,
                        successCount = 1,
                        failedCount = 0,
                        errorMessage = null
                    )
                } else {
                    lastError = createResult.exceptionOrNull() as? Exception ?: Exception("Unknown error")
                    
                    // Determine outcome based on error type
                    val outcome = when (lastError) {
                        is ApiException -> SyncOutcome.FAILURE
                        else -> SyncOutcome.FAILURE
                    }
                    
                    // Update customer status to INACTIVE
                    val failedCustomer = customer.copy(
                        customerStatus = CustomerStatus.INACTIVE,
                        syncError = lastError.message ?: "Unknown error",
                        syncRetryCount = attempt
                    )
                    customerRepository.updateCustomer(failedCustomer)
                    
                    // Retry on failure
                    if (attempt < MAX_RETRIES) {
                        kotlinx.coroutines.delay(RETRY_DELAY_MS * attempt)
                    }
                }
            } catch (e: Exception) {
                lastError = e
                
                val failedCustomer = customer.copy(
                    customerStatus = CustomerStatus.INACTIVE,
                    syncError = e.message ?: "Unknown error",
                    syncRetryCount = attempt
                )
                customerRepository.updateCustomer(failedCustomer)
                
                if (attempt < MAX_RETRIES) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * attempt)
                }
            }
        }
        
        return SyncResult(
            success = false,
            totalSynced = 1,
            successCount = 0,
            failedCount = 1,
            errorMessage = lastError?.message ?: "Max retries exceeded"
        )
    }

    /**
     * Sync a single customer to the router (public method)
     */
    suspend fun syncCustomer(customer: Customer): SyncResult = withContext(Dispatchers.IO) {
        val router = routerRepository.getDefaultEnabledRouter()
            ?: return@withContext SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = "No router configured"
            )

        val log = SyncLog(
            startTime = LocalDateTime.now(),
            totalAttempted = 1,
            status = SyncStatus.SYNCING,
            operationType = SyncOperationType.CREATE,
            customerId = customer.id,
            routerId = router.id
        )
        val logId = syncLogRepository.createSyncLog(log)

        try {
            // Connect to router
            val connectResult = apiClient.connect(router.host, router.port)
            if (connectResult.isFailure) {
                apiClient.disconnect()
                return@withContext createFailedSyncResult(
                    logId,
                    "Failed to connect: ${connectResult.exceptionOrNull()?.message}",
                    SyncOutcome.NETWORK_ERROR
                )
            }

            // Login to router
            val loginResult = apiClient.login(router.username, router.password)
            if (loginResult.isFailure) {
                apiClient.disconnect()
                return@withContext createFailedSyncResult(
                    logId,
                    "Login failed: ${loginResult.exceptionOrNull()?.message}",
                    SyncOutcome.AUTH_ERROR
                )
            }

            // Create PPP secret
            val createResult = apiClient.createPppSecret(
                username = customer.username,
                password = customer.password,
                profile = customer.packageName ?: "default",
                remoteAddress = customer.ipAddress ?: ""
            )

            // Disconnect from router
            apiClient.disconnect()

            val endTime = LocalDateTime.now()
            
            if (createResult.isSuccess) {
                val updatedCustomer = customer.copy(
                    syncStatus = SyncStatus.SYNCED,
                    syncedAt = endTime,
                    syncError = null
                )
                customerRepository.updateCustomer(updatedCustomer)

                syncLogRepository.updateSyncLog(
                    log.copy(
                        id = logId,
                        endTime = endTime,
                        successCount = 1,
                        failedCount = 0,
                        status = SyncStatus.SYNCED,
                        outcome = SyncOutcome.SUCCESS
                    )
                )

                SyncResult(
                    success = true,
                    totalSynced = 1,
                    successCount = 1,
                    failedCount = 0,
                    errorMessage = null
                )
            } else {
                val errorMsg = createResult.exceptionOrNull()?.message ?: "Unknown error"
                val failedCustomer = customer.copy(
                    customerStatus = CustomerStatus.INACTIVE,
                    syncError = errorMsg
                )
                customerRepository.updateCustomer(failedCustomer)

                syncLogRepository.updateSyncLog(
                    log.copy(
                        id = logId,
                        endTime = endTime,
                        successCount = 0,
                        failedCount = 1,
                        status = SyncStatus.FAILED,
                        errorMessage = errorMsg,
                        outcome = SyncOutcome.FAILURE
                    )
                )

                SyncResult(
                    success = false,
                    totalSynced = 1,
                    successCount = 0,
                    failedCount = 1,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            apiClient.disconnect()
            createFailedSyncResult(
                logId,
                "Sync error: ${e.message}",
                SyncOutcome.FAILURE
            )
        }
    }

    /**
     * Enable a customer's PPP secret
     */
    suspend fun enableCustomer(customer: Customer): SyncResult = withContext(Dispatchers.IO) {
        val router = routerRepository.getDefaultEnabledRouter()
            ?: return@withContext SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = "No router configured"
            )

        try {
            val connectResult = apiClient.connect(router.host, router.port)
            if (connectResult.isFailure) {
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = connectResult.exceptionOrNull()?.message
                )
            }

            val loginResult = apiClient.login(router.username, router.password)
            if (loginResult.isFailure) {
                apiClient.disconnect()
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = loginResult.exceptionOrNull()?.message
                )
            }

            val enableResult = apiClient.enableSecret(customer.username)
            apiClient.disconnect()

            if (enableResult.isSuccess) {
                val updatedCustomer = customer.copy(
                    customerStatus = CustomerStatus.ACTIVE,
                    syncError = null
                )
                customerRepository.updateCustomer(updatedCustomer)
                
                SyncResult(
                    success = true,
                    totalSynced = 1,
                    successCount = 1,
                    failedCount = 0,
                    errorMessage = null
                )
            } else {
                SyncResult(
                    success = false,
                    totalSynced = 1,
                    successCount = 0,
                    failedCount = 1,
                    errorMessage = enableResult.exceptionOrNull()?.message
                )
            }
        } catch (e: Exception) {
            apiClient.disconnect()
            SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = e.message
            )
        }
    }

    /**
     * Disable a customer's PPP secret
     */
    suspend fun disableCustomer(customer: Customer): SyncResult = withContext(Dispatchers.IO) {
        val router = routerRepository.getDefaultEnabledRouter()
            ?: return@withContext SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = "No router configured"
            )

        try {
            val connectResult = apiClient.connect(router.host, router.port)
            if (connectResult.isFailure) {
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = connectResult.exceptionOrNull()?.message
                )
            }

            val loginResult = apiClient.login(router.username, router.password)
            if (loginResult.isFailure) {
                apiClient.disconnect()
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = loginResult.exceptionOrNull()?.message
                )
            }

            val disableResult = apiClient.disableSecret(customer.username)
            apiClient.disconnect()

            if (disableResult.isSuccess) {
                val updatedCustomer = customer.copy(
                    customerStatus = CustomerStatus.SUSPENDED,
                    syncError = null
                )
                customerRepository.updateCustomer(updatedCustomer)
                
                SyncResult(
                    success = true,
                    totalSynced = 1,
                    successCount = 1,
                    failedCount = 0,
                    errorMessage = null
                )
            } else {
                SyncResult(
                    success = false,
                    totalSynced = 1,
                    successCount = 0,
                    failedCount = 1,
                    errorMessage = disableResult.exceptionOrNull()?.message
                )
            }
        } catch (e: Exception) {
            apiClient.disconnect()
            SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = e.message
            )
        }
    }

    /**
     * Delete a customer's PPP secret from router
     */
    suspend fun deleteCustomerFromRouter(customer: Customer): SyncResult = withContext(Dispatchers.IO) {
        val router = routerRepository.getDefaultEnabledRouter()
            ?: return@withContext SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = "No router configured"
            )

        try {
            val connectResult = apiClient.connect(router.host, router.port)
            if (connectResult.isFailure) {
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = connectResult.exceptionOrNull()?.message
                )
            }

            val loginResult = apiClient.login(router.username, router.password)
            if (loginResult.isFailure) {
                apiClient.disconnect()
                return@withContext SyncResult(
                    success = false,
                    totalSynced = 0,
                    successCount = 0,
                    failedCount = 0,
                    errorMessage = loginResult.exceptionOrNull()?.message
                )
            }

            val deleteResult = apiClient.deleteSecret(customer.username)
            apiClient.disconnect()

            if (deleteResult.isSuccess) {
                SyncResult(
                    success = true,
                    totalSynced = 1,
                    successCount = 1,
                    failedCount = 0,
                    errorMessage = null
                )
            } else {
                SyncResult(
                    success = false,
                    totalSynced = 1,
                    successCount = 0,
                    failedCount = 1,
                    errorMessage = deleteResult.exceptionOrNull()?.message
                )
            }
        } catch (e: Exception) {
            apiClient.disconnect()
            SyncResult(
                success = false,
                totalSynced = 0,
                successCount = 0,
                failedCount = 0,
                errorMessage = e.message
            )
        }
    }

    private suspend fun createFailedSyncResult(
        logId: Long,
        errorMessage: String,
        outcome: SyncOutcome
    ): SyncResult {
        syncLogRepository.updateSyncLog(
            SyncLog(
                id = logId,
                endTime = LocalDateTime.now(),
                totalAttempted = 0,
                successCount = 0,
                failedCount = 0,
                status = SyncStatus.FAILED,
                errorMessage = errorMessage,
                outcome = outcome
            )
        )

        return SyncResult(
            success = false,
            totalSynced = 0,
            successCount = 0,
            failedCount = 0,
            errorMessage = errorMessage
        )
    }
}

/**
 * Result of a sync operation
 */
data class SyncResult(
    val success: Boolean,
    val totalSynced: Int,
    val successCount: Int,
    val failedCount: Int,
    val errorMessage: String?
) {
    companion object {
        fun empty() = SyncResult(
            success = true,
            totalSynced = 0,
            successCount = 0,
            failedCount = 0,
            errorMessage = null
        )
    }
}
