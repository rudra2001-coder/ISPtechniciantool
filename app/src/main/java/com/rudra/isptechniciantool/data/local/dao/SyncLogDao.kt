package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.SyncLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SyncLog operations.
 */
@Dao
interface SyncLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncLog: SyncLogEntity): Long
    
    @Update
    suspend fun update(syncLog: SyncLogEntity)
    
    @Query("SELECT * FROM sync_logs WHERE id = :logId")
    suspend fun getById(logId: Long): SyncLogEntity?
    
    @Query("SELECT * FROM sync_logs WHERE customer_id = :customerId ORDER BY request_timestamp DESC")
    fun observeByCustomerId(customerId: Long): Flow<List<SyncLogEntity>>
    
    @Query("SELECT * FROM sync_logs ORDER BY start_time DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SyncLogEntity>>
    
    @Query("SELECT * FROM sync_logs WHERE status = 'FAILED' ORDER BY start_time DESC LIMIT :limit")
    fun observeRecentFailures(limit: Int): Flow<List<SyncLogEntity>>
    
    @Query("DELETE FROM sync_logs WHERE customer_id = :customerId")
    suspend fun deleteByCustomerId(customerId: Long)
    
    @Query("DELETE FROM sync_logs WHERE start_time < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM sync_logs WHERE status = 'FAILED'")
    fun observeFailureCount(): Flow<Int>
}
