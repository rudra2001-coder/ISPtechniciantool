package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.RouterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Router operations.
 */
@Dao
interface RouterDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(router: RouterEntity): Long
    
    @Update
    suspend fun update(router: RouterEntity)
    
    @Delete
    suspend fun delete(router: RouterEntity)
    
    @Query("DELETE FROM routers WHERE id = :routerId")
    suspend fun deleteById(routerId: Long)
    
    @Query("SELECT * FROM routers WHERE id = :routerId")
    suspend fun getById(routerId: Long): RouterEntity?
    
    @Query("SELECT * FROM routers WHERE id = :routerId")
    fun observeById(routerId: Long): Flow<RouterEntity?>
    
    @Query("SELECT * FROM routers ORDER BY name ASC")
    fun observeAll(): Flow<List<RouterEntity>>
    
    @Query("SELECT * FROM routers WHERE is_enabled = 1 ORDER BY name ASC")
    fun observeEnabled(): Flow<List<RouterEntity>>
    
    @Query("SELECT * FROM routers WHERE is_enabled = 1 LIMIT 1")
    fun observeDefaultEnabled(): Flow<RouterEntity?>
    
    @Query("SELECT * FROM routers WHERE is_enabled = 1 LIMIT 1")
    suspend fun getDefaultEnabled(): RouterEntity?
    
    @Query("UPDATE routers SET last_successful_connect = :timestamp, modified_at = :modifiedAt WHERE id = :routerId")
    suspend fun updateLastSuccessfulConnect(routerId: Long, timestamp: Long, modifiedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE routers SET is_enabled = :enabled, modified_at = :modifiedAt WHERE id = :routerId")
    suspend fun setEnabled(routerId: Long, enabled: Boolean, modifiedAt: Long = System.currentTimeMillis())
}
