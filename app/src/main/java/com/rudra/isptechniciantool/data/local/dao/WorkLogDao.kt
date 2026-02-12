package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.isptechniciantool.data.local.entity.WorkLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WorkLog operations.
 */
@Dao
interface WorkLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workLog: WorkLogEntity): Long
    
    @Query("SELECT * FROM work_logs WHERE id = :logId")
    suspend fun getById(logId: Long): WorkLogEntity?
    
    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WorkLogEntity>>
    
    @Query("SELECT * FROM work_logs WHERE customer_id = :customerId ORDER BY timestamp DESC")
    fun observeByCustomerId(customerId: Long): Flow<List<WorkLogEntity>>
    
    @Query("SELECT * FROM work_logs WHERE task_id = :taskId ORDER BY timestamp DESC")
    fun observeByTaskId(taskId: Long): Flow<List<WorkLogEntity>>
    
    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WorkLogEntity>>
    
    @Query("DELETE FROM work_logs WHERE id = :logId")
    suspend fun deleteById(logId: Long)
    
    @Query("DELETE FROM work_logs WHERE customer_id = :customerId")
    suspend fun deleteByCustomerId(customerId: Long)
    
    @Query("DELETE FROM work_logs WHERE task_id = :taskId")
    suspend fun deleteByTaskId(taskId: Long)
    
    @Query("SELECT SUM(duration_minutes) FROM work_logs WHERE customer_id = :customerId")
    suspend fun getTotalDurationForCustomer(customerId: Long): Int?
}
