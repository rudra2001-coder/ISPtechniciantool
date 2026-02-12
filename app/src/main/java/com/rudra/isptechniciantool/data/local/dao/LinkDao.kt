package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.LinkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Link operations.
 */
@Dao
interface LinkDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: LinkEntity): Long
    
    @Update
    suspend fun update(link: LinkEntity)
    
    @Delete
    suspend fun delete(link: LinkEntity)
    
    @Query("DELETE FROM links WHERE id = :linkId")
    suspend fun deleteById(linkId: Long)
    
    @Query("SELECT * FROM links WHERE id = :linkId")
    suspend fun getLinkById(linkId: Long): LinkEntity?
    
    @Query("SELECT * FROM links WHERE id = :linkId")
    fun observeLinkById(linkId: Long): Flow<LinkEntity?>
    
    @Query("SELECT * FROM links ORDER BY created_at DESC")
    fun observeAllLinks(): Flow<List<LinkEntity>>
    
    @Query("SELECT * FROM links ORDER BY created_at DESC")
    suspend fun getAllLinks(): List<LinkEntity>
    
    @Query("SELECT * FROM links WHERE source_device_id = :deviceId OR target_device_id = :deviceId ORDER BY created_at DESC")
    suspend fun getLinksByDeviceId(deviceId: Long): List<LinkEntity>
    
    @Query("SELECT * FROM links WHERE source_device_id = :deviceId OR target_device_id = :deviceId ORDER BY created_at DESC")
    fun observeLinksByDeviceId(deviceId: Long): Flow<List<LinkEntity>>
    
    @Query("SELECT * FROM links WHERE (source_device_id = :sourceId AND target_device_id = :targetId) OR (source_device_id = :targetId AND target_device_id = :sourceId) LIMIT 1")
    suspend fun getLinksBetweenDevices(sourceId: Long, targetId: Long): LinkEntity?
    
    @Query("SELECT * FROM links WHERE source_device_id = :deviceId OR target_device_id = :deviceId")
    suspend fun getLinksForDevice(deviceId: Long): List<LinkEntity>
    
    @Query("SELECT * FROM links WHERE status = :status ORDER BY created_at DESC")
    fun observeLinksByStatus(status: String): Flow<List<LinkEntity>>
    
    @Query("SELECT COUNT(*) FROM links")
    suspend fun getLinkCount(): Int
    
    @Query("DELETE FROM links WHERE source_device_id = :deviceId OR target_device_id = :deviceId")
    suspend fun deleteLinksByDeviceId(deviceId: Long)
}
