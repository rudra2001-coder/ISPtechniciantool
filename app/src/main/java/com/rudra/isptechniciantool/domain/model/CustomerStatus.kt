package com.rudra.isptechniciantool.domain.model

/**
 * Represents the service status of a customer.
 */
enum class CustomerStatus {
    /** New customer awaiting installation */
    PENDING,
    
    /** Service is active and functional */
    ACTIVE,
    
    /** Service temporarily suspended */
    SUSPENDED,
    
    /** Service permanently disconnected */
    INACTIVE
}
