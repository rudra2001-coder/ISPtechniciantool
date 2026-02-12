package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.Router
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Router operations.
 */
interface RouterRepository {
    
    suspend fun createRouter(router: Router): Long
    
    suspend fun updateRouter(router: Router)
    
    suspend fun deleteRouter(router: Router)
    
    suspend fun deleteRouterById(routerId: Long)
    
    suspend fun getRouterById(routerId: Long): Router?
    
    fun observeRouterById(routerId: Long): Flow<Router?>
    
    fun observeAllRouters(): Flow<List<Router>>
    
    fun observeEnabledRouters(): Flow<List<Router>>
    
    fun observeDefaultEnabledRouter(): Flow<Router?>
    
    suspend fun getDefaultEnabledRouter(): Router?
    
    suspend fun updateLastSuccessfulConnect(routerId: Long)
    
    suspend fun setRouterEnabled(routerId: Long, enabled: Boolean)
}
