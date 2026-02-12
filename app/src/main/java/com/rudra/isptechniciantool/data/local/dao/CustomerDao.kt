package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Customer operations.
 */
@Dao
interface CustomerDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>): List<Long>
    
    @Update
    suspend fun update(customer: CustomerEntity)
    
    @Delete
    suspend fun delete(customer: CustomerEntity)
    
    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteById(customerId: Long)
    
    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getById(customerId: Long): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun observeById(customerId: Long): Flow<CustomerEntity?>
    
    @Query("SELECT * FROM customers ORDER BY created_at DESC")
    fun observeAll(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllPaged(limit: Int, offset: Int): List<CustomerEntity>
    
    @Query("SELECT * FROM customers WHERE sync_status = :syncStatus ORDER BY modified_at ASC")
    fun observeBySyncStatus(syncStatus: String): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE sync_status = :syncStatus ORDER BY modified_at ASC")
    suspend fun getBySyncStatus(syncStatus: String): List<CustomerEntity>
    
    @Query("SELECT * FROM customers WHERE sync_status IN ('PENDING', 'FAILED') ORDER BY modified_at ASC")
    suspend fun getPendingSync(): List<CustomerEntity>
    
    @Query("SELECT * FROM customers WHERE sync_status = 'FAILED' ORDER BY modified_at DESC")
    fun observeFailedSync(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE customer_status = :status ORDER BY name ASC")
    fun observeByStatus(status: String): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE customer_status = :status ORDER BY modified_at ASC")
    suspend fun getByStatus(status: String): List<CustomerEntity>
    
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): CustomerEntity?
    
    @Query("UPDATE customers SET sync_status = :syncStatus, sync_error = :error, sync_retry_count = sync_retry_count + 1, modified_at = :modifiedAt WHERE id = :customerId")
    suspend fun updateSyncStatus(customerId: Long, syncStatus: String, error: String?, modifiedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE customers SET sync_status = 'SYNCED', synced_at = :syncedAt, modified_at = :modifiedAt, sync_error = NULL, sync_retry_count = 0 WHERE id = :customerId")
    suspend fun markSynced(customerId: Long, syncedAt: Long = System.currentTimeMillis(), modifiedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM customers")
    fun observeTotalCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM customers WHERE customer_status = 'ACTIVE'")
    fun observeActiveCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM customers WHERE sync_status = 'PENDING'")
    fun observePendingSyncCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM customers WHERE sync_status = 'FAILED'")
    fun observeFailedSyncCount(): Flow<Int>
    
    @Query("SELECT * FROM customers ORDER BY modified_at DESC LIMIT :limit")
    fun observeRecentlyModified(limit: Int): Flow<List<CustomerEntity>>
}
