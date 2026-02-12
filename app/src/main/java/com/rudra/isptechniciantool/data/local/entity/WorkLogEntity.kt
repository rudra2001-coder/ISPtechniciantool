package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.WorkActivityType
import com.rudra.isptechniciantool.domain.model.WorkLog
import java.time.LocalDateTime

/**
 * Room entity representing a work log entry for tracking technician activities.
 */
@Entity(
    tableName = "work_logs",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["task_id"]),
        Index(value = ["timestamp"])
    ]
)
data class WorkLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,
    
    @ColumnInfo(name = "task_id")
    val taskId: Long? = null,
    
    @ColumnInfo(name = "activity_type")
    val activityType: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null
) {
    fun toDomainModel(): WorkLog {
        return WorkLog(
            id = id,
            customerId = customerId,
            taskId = taskId,
            activityType = WorkActivityType.valueOf(activityType),
            description = description,
            durationMinutes = durationMinutes,
            timestamp = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            photoPath = photoPath
        )
    }
    
    companion object {
        fun fromDomainModel(workLog: WorkLog): WorkLogEntity {
            return WorkLogEntity(
                id = workLog.id,
                customerId = workLog.customerId,
                taskId = workLog.taskId,
                activityType = workLog.activityType.name,
                description = workLog.description,
                durationMinutes = workLog.durationMinutes,
                timestamp = workLog.timestamp
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                photoPath = workLog.photoPath
            )
        }
    }
}
