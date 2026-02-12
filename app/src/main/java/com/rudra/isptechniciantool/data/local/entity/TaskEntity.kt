package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskPriority
import com.rudra.isptechniciantool.domain.model.TaskStatus
import java.time.LocalDateTime

/**
 * Room entity representing a task for the technician.
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["status"]),
        Index(value = ["due_date"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,
    
    @ColumnInfo(name = "priority")
    val priority: String = TaskPriority.MEDIUM.name,
    
    @ColumnInfo(name = "status")
    val status: String = TaskStatus.PENDING.name,
    
    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            customerId = customerId,
            priority = TaskPriority.valueOf(priority),
            status = TaskStatus.valueOf(status),
            dueDate = dueDate?.let {
                java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            },
            completedAt = completedAt?.let {
                java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            },
            createdAt = java.time.Instant.ofEpochMilli(createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            modifiedAt = java.time.Instant.ofEpochMilli(modifiedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        )
    }
    
    companion object {
        fun fromDomainModel(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                customerId = task.customerId,
                priority = task.priority.name,
                status = task.status.name,
                dueDate = task.dueDate?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                completedAt = task.completedAt?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                createdAt = task.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                modifiedAt = task.modifiedAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
    }
}
