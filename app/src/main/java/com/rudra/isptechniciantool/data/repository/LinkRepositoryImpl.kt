package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.LinkDao
import com.rudra.isptechniciantool.data.local.entity.LinkEntity
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkStatus
import com.rudra.isptechniciantool.domain.repository.LinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LinkRepository using Room database.
 */
@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkDao: LinkDao
) : LinkRepository {
    
    override suspend fun createLink(link: Link): Long {
        return linkDao.insert(LinkEntity.fromDomainModel(link))
    }
    
    override suspend fun updateLink(link: Link) {
        linkDao.update(LinkEntity.fromDomainModel(link))
    }
    
    override suspend fun deleteLink(link: Link) {
        linkDao.delete(LinkEntity.fromDomainModel(link))
    }
    
    override suspend fun deleteLinkById(linkId: Long) {
        linkDao.deleteById(linkId)
    }
    
    override suspend fun getLinkById(linkId: Long): Link? {
        return linkDao.getLinkById(linkId)?.toDomainModel()
    }
    
    override fun observeLinkById(linkId: Long): Flow<Link?> {
        return linkDao.observeLinkById(linkId).map { it?.toDomainModel() }
    }
    
    override fun observeAllLinks(): Flow<List<Link>> {
        return linkDao.observeAllLinks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getAllLinks(): List<Link> {
        return linkDao.getAllLinks().map { it.toDomainModel() }
    }
    
    override suspend fun getLinksByDeviceId(deviceId: Long): List<Link> {
        return linkDao.getLinksByDeviceId(deviceId).map { it.toDomainModel() }
    }
    
    override fun observeLinksByDeviceId(deviceId: Long): Flow<List<Link>> {
        return linkDao.observeLinksByDeviceId(deviceId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getLinksBetweenDevices(sourceId: Long, targetId: Long): Link? {
        return linkDao.getLinksBetweenDevices(sourceId, targetId)?.toDomainModel()
    }
    
    override suspend fun getLinksForDevice(deviceId: Long): List<Link> {
        return linkDao.getLinksForDevice(deviceId).map { it.toDomainModel() }
    }
    
    override fun observeLinksByStatus(status: LinkStatus): Flow<List<Link>> {
        return linkDao.observeLinksByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getLinkCount(): Int {
        return linkDao.getLinkCount()
    }
    
    override suspend fun deleteLinksByDeviceId(deviceId: Long) {
        linkDao.deleteLinksByDeviceId(deviceId)
    }
}
