package com.rudra.isptechniciantool.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a work log entry for tracking technician activities.
 */
data class WorkLog(
    val id: Long = 0,
    val customerId: Long? = null,
    val taskId: Long? = null,
    val activityType: WorkActivityType,
    val description: String,
    val durationMinutes: Int? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val photoPath: String? = null
)

enum class WorkActivityType {
    INSTALLATION,
    MAINTENANCE,
    TROUBLESHOOTING,
    BILLING_INQUIRY,
    EQUIPMENT_REPLACEMENT,
    FOLLOW_UP,
    OTHER
}
