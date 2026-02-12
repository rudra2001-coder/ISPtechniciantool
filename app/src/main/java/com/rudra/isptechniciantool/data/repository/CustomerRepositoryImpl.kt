package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.CustomerDao
import com.rudra.isptechniciantool.data.local.entity.CustomerEntity
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CustomerRepository using Room database.
 */
@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {
    
    override suspend fun createCustomer(customer: Customer): Long {
        return customerDao.insert(CustomerEntity.fromDomainModel(customer))
    }
    
    override suspend fun updateCustomer(customer: Customer) {
        customerDao.update(CustomerEntity.fromDomainModel(customer))
    }
    
    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.delete(CustomerEntity.fromDomainModel(customer))
    }
    
    override suspend fun deleteCustomerById(customerId: Long) {
        customerDao.deleteById(customerId)
    }
    
    override suspend fun getCustomerById(customerId: Long): Customer? {
        return customerDao.getById(customerId)?.toDomainModel()
    }
    
    override fun observeCustomerById(customerId: Long): Flow<Customer?> {
        return customerDao.observeById(customerId).map { it?.toDomainModel() }
    }
    
    override fun observeAllCustomers(): Flow<List<Customer>> {
        return customerDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getAllCustomersPaged(limit: Int, offset: Int): List<Customer> {
        return customerDao.getAllPaged(limit, offset).map { it.toDomainModel() }
    }
    
    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<Customer>> {
        return customerDao.observeBySyncStatus(syncStatus.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCustomersBySyncStatus(syncStatus: SyncStatus): List<Customer> {
        return customerDao.getBySyncStatus(syncStatus.name).map { it.toDomainModel() }
    }
    
    override suspend fun getPendingSyncCustomers(): List<Customer> {
        return customerDao.getPendingSync().map { it.toDomainModel() }
    }
    
    override fun observeFailedSyncCustomers(): Flow<List<Customer>> {
        return customerDao.observeFailedSync().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeByCustomerStatus(status: CustomerStatus): Flow<List<Customer>> {
        return customerDao.observeByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCustomersByStatus(status: CustomerStatus): List<Customer> {
        return customerDao.getByStatus(status.name).map { it.toDomainModel() }
    }
    
    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.search(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCustomerByUsername(username: String): Customer? {
        return customerDao.getByUsername(username)?.toDomainModel()
    }
    
    override suspend fun updateSyncStatus(customerId: Long, syncStatus: SyncStatus, error: String?) {
        customerDao.updateSyncStatus(customerId, syncStatus.name, error)
    }
    
    override suspend fun markCustomerSynced(customerId: Long) {
        customerDao.markSynced(customerId)
    }
    
    override fun observeTotalCustomerCount(): Flow<Int> {
        return customerDao.observeTotalCount()
    }
    
    override fun observeActiveCustomerCount(): Flow<Int> {
        return customerDao.observeActiveCount()
    }
    
    override fun observePendingSyncCount(): Flow<Int> {
        return customerDao.observePendingSyncCount()
    }
    
    override fun observeFailedSyncCount(): Flow<Int> {
        return customerDao.observeFailedSyncCount()
    }
    
    override fun observeRecentlyModifiedCustomers(limit: Int): Flow<List<Customer>> {
        return customerDao.observeRecentlyModified(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}
