package com.rudra.isptechniciantool.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.isptechniciantool.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task operations.
 */
@Dao
interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long
    
    @Update
    suspend fun update(task: TaskEntity)
    
    @Delete
    suspend fun delete(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: Long): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeById(taskId: Long): Flow<TaskEntity?>
    
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY due_date ASC")
    fun observeByStatus(status: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status IN ('PENDING', 'IN_PROGRESS') ORDER BY priority DESC, due_date ASC")
    fun observePendingTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = 'OVERDUE' ORDER BY due_date ASC")
    fun observeOverdueTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun observeByCustomerId(customerId: Long): Flow<List<TaskEntity>>
    
    @Query("UPDATE tasks SET status = :status, modified_at = :modifiedAt WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, status: String, modifiedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE tasks SET status = 'COMPLETED', completed_at = :completedAt, modified_at = :modifiedAt WHERE id = :taskId")
    suspend fun markCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis(), modifiedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'OVERDUE'")
    fun observeOverdueCount(): Flow<Int>
}
