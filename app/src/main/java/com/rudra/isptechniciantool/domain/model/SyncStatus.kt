package com.rudra.isptechniciantool.domain.model

/**
 * Represents the synchronization status of a customer record.
 * Used to track whether the customer has been synced to MikroTik router.
 */
enum class SyncStatus {
    /** Customer created locally, not yet synced to MikroTik */
    PENDING,
    
    /** Successfully created PPP secret on MikroTik */
    SYNCED,
    
    /** Sync attempt failed, requires technician attention */
    FAILED,
    
    /** Local and router configurations differ, requires manual resolution */
    CONFLICT,
    
    /** Customer marked for deletion, pending router sync */
    DELETING,
    
    /** Currently being synchronized */
    SYNCING
}
