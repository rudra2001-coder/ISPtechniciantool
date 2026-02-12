package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.WorkLogDao
import com.rudra.isptechniciantool.data.local.entity.WorkLogEntity
import com.rudra.isptechniciantool.domain.model.WorkLog
import com.rudra.isptechniciantool.domain.repository.WorkLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WorkLogRepository using Room database.
 */
@Singleton
class WorkLogRepositoryImpl @Inject constructor(
    private val workLogDao: WorkLogDao
) : WorkLogRepository {
    
    override suspend fun createWorkLog(workLog: WorkLog): Long {
        return workLogDao.insert(WorkLogEntity.fromDomainModel(workLog))
    }
    
    override suspend fun getWorkLogById(logId: Long): WorkLog? {
        return workLogDao.getById(logId)?.toDomainModel()
    }
    
    override fun observeAllWorkLogs(): Flow<List<WorkLog>> {
        return workLogDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeWorkLogsByCustomerId(customerId: Long): Flow<List<WorkLog>> {
        return workLogDao.observeByCustomerId(customerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeWorkLogsByTaskId(taskId: Long): Flow<List<WorkLog>> {
        return workLogDao.observeByTaskId(taskId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeRecentWorkLogs(limit: Int): Flow<List<WorkLog>> {
        return workLogDao.observeRecent(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun deleteWorkLogById(logId: Long) {
        workLogDao.deleteById(logId)
    }
    
    override suspend fun deleteWorkLogsByCustomerId(customerId: Long) {
        workLogDao.deleteByCustomerId(customerId)
    }
    
    override suspend fun deleteWorkLogsByTaskId(taskId: Long) {
        workLogDao.deleteByTaskId(taskId)
    }
    
    override suspend fun getTotalDurationForCustomer(customerId: Long): Int? {
        return workLogDao.getTotalDurationForCustomer(customerId)
    }
}
