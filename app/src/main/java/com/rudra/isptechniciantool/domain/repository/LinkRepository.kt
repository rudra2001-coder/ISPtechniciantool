package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Link operations.
 */
interface LinkRepository {
    
    suspend fun createLink(link: Link): Long
    
    suspend fun updateLink(link: Link)
    
    suspend fun deleteLink(link: Link)
    
    suspend fun deleteLinkById(linkId: Long)
    
    suspend fun getLinkById(linkId: Long): Link?
    
    fun observeLinkById(linkId: Long): Flow<Link?>
    
    fun observeAllLinks(): Flow<List<Link>>
    
    suspend fun getAllLinks(): List<Link>
    
    suspend fun getLinksByDeviceId(deviceId: Long): List<Link>
    
    fun observeLinksByDeviceId(deviceId: Long): Flow<List<Link>>
    
    suspend fun getLinksBetweenDevices(sourceId: Long, targetId: Long): Link?
    
    suspend fun getLinksForDevice(deviceId: Long): List<Link>
    
    fun observeLinksByStatus(status: LinkStatus): Flow<List<Link>>
    
    suspend fun getLinkCount(): Int
    
    suspend fun deleteLinksByDeviceId(deviceId: Long)
}
