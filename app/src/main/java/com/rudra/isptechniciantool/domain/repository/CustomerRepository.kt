package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Customer operations.
 * Defines the contract between domain and data layers.
 */
interface CustomerRepository {
    
    suspend fun createCustomer(customer: Customer): Long
    
    suspend fun updateCustomer(customer: Customer)
    
    suspend fun deleteCustomer(customer: Customer)
    
    suspend fun deleteCustomerById(customerId: Long)
    
    suspend fun getCustomerById(customerId: Long): Customer?
    
    fun observeCustomerById(customerId: Long): Flow<Customer?>
    
    fun observeAllCustomers(): Flow<List<Customer>>
    
    suspend fun getAllCustomersPaged(limit: Int, offset: Int): List<Customer>
    
    fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<Customer>>
    
    suspend fun getCustomersBySyncStatus(syncStatus: SyncStatus): List<Customer>
    
    suspend fun getPendingSyncCustomers(): List<Customer>
    
    fun observeFailedSyncCustomers(): Flow<List<Customer>>
    
    fun observeByCustomerStatus(status: CustomerStatus): Flow<List<Customer>>
    
    suspend fun getCustomersByStatus(status: CustomerStatus): List<Customer>
    
    fun searchCustomers(query: String): Flow<List<Customer>>
    
    suspend fun getCustomerByUsername(username: String): Customer?
    
    suspend fun updateSyncStatus(customerId: Long, syncStatus: SyncStatus, error: String?)
    
    suspend fun markCustomerSynced(customerId: Long)
    
    fun observeTotalCustomerCount(): Flow<Int>
    
    fun observeActiveCustomerCount(): Flow<Int>
    
    fun observePendingSyncCount(): Flow<Int>
    
    fun observeFailedSyncCount(): Flow<Int>
    
    fun observeRecentlyModifiedCustomers(limit: Int): Flow<List<Customer>>
}
