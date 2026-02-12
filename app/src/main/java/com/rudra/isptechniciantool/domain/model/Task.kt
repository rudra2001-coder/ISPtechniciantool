package com.rudra.isptechniciantool.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a task/job for the technician.
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val customerId: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    OVERDUE
}
