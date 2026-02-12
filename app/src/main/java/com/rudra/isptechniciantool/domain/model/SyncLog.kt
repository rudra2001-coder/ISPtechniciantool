package com.rudra.isptechniciantool.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a synchronization log entry.
 */
data class SyncLog(
    val id: Long = 0,
    val customerId: Long = 0,
    val routerId: Long = 0,
    val operationType: SyncOperationType = SyncOperationType.CREATE,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val totalAttempted: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val status: SyncStatus = SyncStatus.PENDING,
    val errorMessage: String? = null,
    val requestTimestamp: LocalDateTime = LocalDateTime.now(),
    val responseTimestamp: LocalDateTime? = null,
    val outcome: SyncOutcome = SyncOutcome.SUCCESS,
    val errorCode: String? = null,
    val requestData: String? = null,
    val responseData: String? = null
)

enum class SyncOperationType {
    CREATE,
    UPDATE,
    DELETE,
    ENABLE,
    DISABLE,
    BATCH_SYNC
}

enum class SyncOutcome {
    SUCCESS,
    FAILURE,
    TIMEOUT,
    AUTH_ERROR,
    NETWORK_ERROR
}
