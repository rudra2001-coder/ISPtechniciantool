package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.DeviceDao
import com.rudra.isptechniciantool.data.local.entity.DeviceEntity
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceType
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DeviceRepository using Room database.
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao
) : DeviceRepository {
    
    override suspend fun createDevice(device: Device): Long {
        return deviceDao.insert(DeviceEntity.fromDomainModel(device))
    }
    
    override suspend fun updateDevice(device: Device) {
        deviceDao.update(DeviceEntity.fromDomainModel(device))
    }
    
    override suspend fun deleteDevice(device: Device) {
        deviceDao.delete(DeviceEntity.fromDomainModel(device))
    }
    
    override suspend fun deleteDeviceById(deviceId: Long) {
        deviceDao.deleteById(deviceId)
    }
    
    override suspend fun getDeviceById(deviceId: Long): Device? {
        return deviceDao.getDeviceById(deviceId)?.toDomainModel()
    }
    
    override fun observeDeviceById(deviceId: Long): Flow<Device?> {
        return deviceDao.observeDeviceById(deviceId).map { it?.toDomainModel() }
    }
    
    override fun observeAllDevices(): Flow<List<Device>> {
        return deviceDao.observeAllDevices().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getAllDevices(): List<Device> {
        return deviceDao.getAllDevices().map { it.toDomainModel() }
    }
    
    override suspend fun getDevicesByType(type: DeviceType): List<Device> {
        return deviceDao.getDevicesByType(type.name).map { it.toDomainModel() }
    }
    
    override fun observeDevicesByType(type: DeviceType): Flow<List<Device>> {
        return deviceDao.observeDevicesByType(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getDevicesWithCoordinates(): List<Device> {
        return deviceDao.getDevicesWithCoordinates().map { it.toDomainModel() }
    }
    
    override fun observeDevicesWithCoordinates(): Flow<List<Device>> {
        return deviceDao.observeDevicesWithCoordinates().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getDevicesForMonitoring(): List<Device> {
        return deviceDao.getDevicesForMonitoring().map { it.toDomainModel() }
    }
    
    override fun observeDevicesForMonitoring(): Flow<List<Device>> {
        return deviceDao.observeDevicesForMonitoring().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun updateDeviceStatus(deviceId: Long, status: String, lastSeen: Long) {
        deviceDao.updateStatus(deviceId, status, lastSeen)
    }
    
    override suspend fun getDeviceCount(): Int {
        return deviceDao.getDeviceCount()
    }
    
    override suspend fun getDeviceCountByStatus(status: String): Int {
        return deviceDao.getDeviceCountByStatus(status)
    }
}
