package com.rudra.isptechniciantool.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing an ISP customer.
 * This is the clean domain representation used throughout the application.
 */
data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val phoneSecondary: String? = null,
    val email: String? = null,
    val address: String,
    val addressNotes: String? = null,
    val username: String,
    val password: String,
    val ipAddress: String? = null,
    val packageId: Long? = null,
    val packageName: String? = null,
    val customerStatus: CustomerStatus = CustomerStatus.PENDING,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val installDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val syncedAt: LocalDateTime? = null,
    val notes: String? = null,
    val routerId: Long? = null,
    val syncError: String? = null,
    val syncRetryCount: Int = 0
)
