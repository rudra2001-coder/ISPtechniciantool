package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.WorkLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for WorkLog operations.
 */
interface WorkLogRepository {
    
    suspend fun createWorkLog(workLog: WorkLog): Long
    
    suspend fun getWorkLogById(logId: Long): WorkLog?
    
    fun observeAllWorkLogs(): Flow<List<WorkLog>>
    
    fun observeWorkLogsByCustomerId(customerId: Long): Flow<List<WorkLog>>
    
    fun observeWorkLogsByTaskId(taskId: Long): Flow<List<WorkLog>>
    
    fun observeRecentWorkLogs(limit: Int): Flow<List<WorkLog>>
    
    suspend fun deleteWorkLogById(logId: Long)
    
    suspend fun deleteWorkLogsByCustomerId(customerId: Long)
    
    suspend fun deleteWorkLogsByTaskId(taskId: Long)
    
    suspend fun getTotalDurationForCustomer(customerId: Long): Int?
}
