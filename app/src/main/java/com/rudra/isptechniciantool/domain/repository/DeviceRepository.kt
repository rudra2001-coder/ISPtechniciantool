package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Device operations.
 */
interface DeviceRepository {
    
    suspend fun createDevice(device: Device): Long
    
    suspend fun updateDevice(device: Device)
    
    suspend fun deleteDevice(device: Device)
    
    suspend fun deleteDeviceById(deviceId: Long)
    
    suspend fun getDeviceById(deviceId: Long): Device?
    
    fun observeDeviceById(deviceId: Long): Flow<Device?>
    
    fun observeAllDevices(): Flow<List<Device>>
    
    suspend fun getAllDevices(): List<Device>
    
    suspend fun getDevicesByType(type: DeviceType): List<Device>
    
    fun observeDevicesByType(type: DeviceType): Flow<List<Device>>
    
    suspend fun getDevicesWithCoordinates(): List<Device>
    
    fun observeDevicesWithCoordinates(): Flow<List<Device>>
    
    suspend fun getDevicesForMonitoring(): List<Device>
    
    fun observeDevicesForMonitoring(): Flow<List<Device>>
    
    suspend fun updateDeviceStatus(deviceId: Long, status: String, lastSeen: Long)
    
    suspend fun getDeviceCount(): Int
    
    suspend fun getDeviceCountByStatus(status: String): Int
}
