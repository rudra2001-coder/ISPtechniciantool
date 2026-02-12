package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.RouterDao
import com.rudra.isptechniciantool.data.local.entity.RouterEntity
import com.rudra.isptechniciantool.domain.model.Router
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RouterRepository using Room database.
 */
@Singleton
class RouterRepositoryImpl @Inject constructor(
    private val routerDao: RouterDao
) : RouterRepository {
    
    override suspend fun createRouter(router: Router): Long {
        return routerDao.insert(RouterEntity.fromDomainModel(router))
    }
    
    override suspend fun updateRouter(router: Router) {
        routerDao.update(RouterEntity.fromDomainModel(router))
    }
    
    override suspend fun deleteRouter(router: Router) {
        routerDao.delete(RouterEntity.fromDomainModel(router))
    }
    
    override suspend fun deleteRouterById(routerId: Long) {
        routerDao.deleteById(routerId)
    }
    
    override suspend fun getRouterById(routerId: Long): Router? {
        return routerDao.getById(routerId)?.toDomainModel()
    }
    
    override fun observeRouterById(routerId: Long): Flow<Router?> {
        return routerDao.observeById(routerId).map { it?.toDomainModel() }
    }
    
    override fun observeAllRouters(): Flow<List<Router>> {
        return routerDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeEnabledRouters(): Flow<List<Router>> {
        return routerDao.observeEnabled().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeDefaultEnabledRouter(): Flow<Router?> {
        return routerDao.observeDefaultEnabled().map { it?.toDomainModel() }
    }
    
    override suspend fun getDefaultEnabledRouter(): Router? {
        return routerDao.getDefaultEnabled()?.toDomainModel()
    }
    
    override suspend fun updateLastSuccessfulConnect(routerId: Long) {
        routerDao.updateLastSuccessfulConnect(routerId, System.currentTimeMillis())
    }
    
    override suspend fun setRouterEnabled(routerId: Long, enabled: Boolean) {
        routerDao.setEnabled(routerId, enabled)
    }
}