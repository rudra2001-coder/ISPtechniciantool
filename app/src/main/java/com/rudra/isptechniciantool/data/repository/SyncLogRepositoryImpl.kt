package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.SyncLogDao
import com.rudra.isptechniciantool.data.local.entity.SyncLogEntity
import com.rudra.isptechniciantool.domain.model.SyncLog
import com.rudra.isptechniciantool.domain.repository.SyncLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncLogRepository using Room database.
 */
@Singleton
class SyncLogRepositoryImpl @Inject constructor(
    private val syncLogDao: SyncLogDao
) : SyncLogRepository {
    
    override suspend fun createSyncLog(syncLog: SyncLog): Long {
        return syncLogDao.insert(SyncLogEntity.fromDomainModel(syncLog))
    }
    
    override suspend fun updateSyncLog(syncLog: SyncLog) {
        syncLogDao.update(SyncLogEntity.fromDomainModel(syncLog))
    }
    
    override suspend fun getSyncLogById(logId: Long): SyncLog? {
        return syncLogDao.getById(logId)?.toDomainModel()
    }
    
    override fun observeSyncLogsByCustomerId(customerId: Long): Flow<List<SyncLog>> {
        return syncLogDao.observeByCustomerId(customerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeRecentSyncLogs(limit: Int): Flow<List<SyncLog>> {
        return syncLogDao.observeRecent(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeRecentFailedSyncLogs(limit: Int): Flow<List<SyncLog>> {
        return syncLogDao.observeRecentFailures(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun deleteSyncLogsByCustomerId(customerId: Long) {
        syncLogDao.deleteByCustomerId(customerId)
    }
    
    override suspend fun deleteOldSyncLogs(beforeTimestamp: Long) {
        syncLogDao.deleteOldLogs(beforeTimestamp)
    }
    
    override fun observeFailedSyncLogCount(): Flow<Int> {
        return syncLogDao.observeFailureCount()
    }
}
