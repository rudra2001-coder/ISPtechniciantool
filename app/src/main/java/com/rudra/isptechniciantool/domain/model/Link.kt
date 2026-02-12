package com.rudra.isptechniciantool.domain.model

/**
 * Domain model representing a connection between two network devices.
 */
data class Link(
    val id: Long = 0,
    val sourceDeviceId: Long,
    val targetDeviceId: Long,
    val linkType: LinkType = LinkType.FIBER,
    val bandwidth: String? = null,
    val status: LinkStatus = LinkStatus.ACTIVE,
    val color: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing different types of network links.
 */
enum class LinkType {
    FIBER,
    COPPER,
    WIRELESS,
    VLAN,
    VPN,
    LTE,
    SATELLITE,
    OTHER
}

/**
 * Enum representing the status of a network link.
 */
enum class LinkStatus {
    ACTIVE,
    INACTIVE,
    DEGRADED,
    DOWN,
    UNKNOWN
}
