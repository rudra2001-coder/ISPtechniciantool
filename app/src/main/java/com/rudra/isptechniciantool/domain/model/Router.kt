package com.rudra.isptechniciantool.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a MikroTik router configuration.
 */
data class Router(
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 8728,
    val username: String,
    val password: String,
    val timeout: Int = 3000,
    val isEnabled: Boolean = true,
    val lastSuccessfulConnect: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)
