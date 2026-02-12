package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.SyncLog
import com.rudra.isptechniciantool.domain.model.SyncOperationType
import com.rudra.isptechniciantool.domain.model.SyncOutcome
import com.rudra.isptechniciantool.domain.model.SyncStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Room entity representing a synchronization log entry.
 */
@Entity(
    tableName = "sync_logs",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RouterEntity::class,
            parentColumns = ["id"],
            childColumns = ["router_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["router_id"]),
        Index(value = ["request_timestamp"])
    ]
)
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "customer_id")
    val customerId: Long = 0,
    
    @ColumnInfo(name = "router_id")
    val routerId: Long? = null,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String = SyncOperationType.CREATE.name,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,
    
    @ColumnInfo(name = "total_attempted")
    val totalAttempted: Int = 0,
    
    @ColumnInfo(name = "success_count")
    val successCount: Int = 0,
    
    @ColumnInfo(name = "failed_count")
    val failedCount: Int = 0,
    
    @ColumnInfo(name = "status")
    val status: String = SyncStatus.PENDING.name,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "request_timestamp")
    val requestTimestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "response_timestamp")
    val responseTimestamp: Long? = null,
    
    @ColumnInfo(name = "outcome")
    val outcome: String = SyncOutcome.SUCCESS.name,
    
    @ColumnInfo(name = "error_code")
    val errorCode: String? = null,
    
    @ColumnInfo(name = "request_data")
    val requestData: String? = null,
    
    @ColumnInfo(name = "response_data")
    val responseData: String? = null
) {
    fun toDomainModel(): SyncLog {
        return SyncLog(
            id = id,
            customerId = customerId,
            routerId = routerId ?: 0,
            operationType = try {
                SyncOperationType.valueOf(operationType)
            } catch (e: Exception) {
                SyncOperationType.CREATE
            },
            startTime = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDateTime(),
            endTime = endTime?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            },
            totalAttempted = totalAttempted,
            successCount = successCount,
            failedCount = failedCount,
            status = try {
                SyncStatus.valueOf(status)
            } catch (e: Exception) {
                SyncStatus.PENDING
            },
            errorMessage = errorMessage,
            requestTimestamp = Instant.ofEpochMilli(requestTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime(),
            responseTimestamp = responseTimestamp?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            },
            outcome = try {
                SyncOutcome.valueOf(outcome)
            } catch (e: Exception) {
                SyncOutcome.SUCCESS
            },
            errorCode = errorCode,
            requestData = requestData,
            responseData = responseData
        )
    }
    
    companion object {
        fun fromDomainModel(syncLog: SyncLog): SyncLogEntity {
            return SyncLogEntity(
                id = syncLog.id,
                customerId = syncLog.customerId,
                routerId = syncLog.routerId,
                operationType = syncLog.operationType.name,
                startTime = syncLog.startTime.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
                endTime = syncLog.endTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                totalAttempted = syncLog.totalAttempted,
                successCount = syncLog.successCount,
                failedCount = syncLog.failedCount,
                status = syncLog.status.name,
                errorMessage = syncLog.errorMessage,
                requestTimestamp = syncLog.requestTimestamp.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
                responseTimestamp = syncLog.responseTimestamp?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                outcome = syncLog.outcome.name,
                errorCode = syncLog.errorCode,
                requestData = syncLog.requestData,
                responseData = syncLog.responseData
            )
        }
    }
}
