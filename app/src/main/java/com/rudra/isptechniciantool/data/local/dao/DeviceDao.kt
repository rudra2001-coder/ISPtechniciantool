package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Device operations.
 */
@Dao
interface DeviceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity): Long
    
    @Update
    suspend fun update(device: DeviceEntity)
    
    @Delete
    suspend fun delete(device: DeviceEntity)
    
    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteById(deviceId: Long)
    
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: Long): DeviceEntity?
    
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    fun observeDeviceById(deviceId: Long): Flow<DeviceEntity?>
    
    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun observeAllDevices(): Flow<List<DeviceEntity>>
    
    @Query("SELECT * FROM devices ORDER BY name ASC")
    suspend fun getAllDevices(): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE device_type = :type ORDER BY name ASC")
    suspend fun getDevicesByType(type: String): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE device_type = :type ORDER BY name ASC")
    fun observeDevicesByType(type: String): Flow<List<DeviceEntity>>
    
    @Query("SELECT * FROM devices WHERE latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY name ASC")
    suspend fun getDevicesWithCoordinates(): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY name ASC")
    fun observeDevicesWithCoordinates(): Flow<List<DeviceEntity>>
    
    @Query("SELECT * FROM devices WHERE monitoring_enabled = 1 ORDER BY name ASC")
    suspend fun getDevicesForMonitoring(): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE monitoring_enabled = 1 ORDER BY name ASC")
    fun observeDevicesForMonitoring(): Flow<List<DeviceEntity>>
    
    @Query("UPDATE devices SET status = :status, last_seen = :lastSeen, updated_at = :updatedAt WHERE id = :deviceId")
    suspend fun updateStatus(deviceId: Long, status: String, lastSeen: Long, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM devices")
    suspend fun getDeviceCount(): Int
    
    @Query("SELECT COUNT(*) FROM devices WHERE status = :status")
    suspend fun getDeviceCountByStatus(status: String): Int
}
