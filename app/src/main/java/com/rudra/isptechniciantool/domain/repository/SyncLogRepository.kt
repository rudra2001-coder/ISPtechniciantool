package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.SyncLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for SyncLog operations.
 */
interface SyncLogRepository {
    
    suspend fun createSyncLog(syncLog: SyncLog): Long
    
    suspend fun updateSyncLog(syncLog: SyncLog)
    
    suspend fun getSyncLogById(logId: Long): SyncLog?
    
    fun observeSyncLogsByCustomerId(customerId: Long): Flow<List<SyncLog>>
    
    fun observeRecentSyncLogs(limit: Int): Flow<List<SyncLog>>
    
    fun observeRecentFailedSyncLogs(limit: Int): Flow<List<SyncLog>>
    
    suspend fun deleteSyncLogsByCustomerId(customerId: Long)
    
    suspend fun deleteOldSyncLogs(beforeTimestamp: Long)
    
    fun observeFailedSyncLogCount(): Flow<Int>
}
