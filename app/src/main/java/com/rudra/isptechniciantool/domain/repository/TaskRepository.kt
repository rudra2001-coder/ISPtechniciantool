package com.rudra.isptechniciantool.domain.repository

import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task operations.
 */
interface TaskRepository {
    
    suspend fun createTask(task: Task): Long
    
    suspend fun updateTask(task: Task)
    
    suspend fun deleteTask(task: Task)
    
    suspend fun deleteTaskById(taskId: Long)
    
    suspend fun getTaskById(taskId: Long): Task?
    
    fun observeTaskById(taskId: Long): Flow<Task?>
    
    fun observeAllTasks(): Flow<List<Task>>
    
    fun observeTasksByStatus(status: TaskStatus): Flow<List<Task>>
    
    fun observePendingTasks(): Flow<List<Task>>
    
    fun observeOverdueTasks(): Flow<List<Task>>
    
    fun observeTasksByCustomerId(customerId: Long): Flow<List<Task>>
    
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus)
    
    suspend fun markTaskCompleted(taskId: Long)
    
    fun observePendingTaskCount(): Flow<Int>
    
    fun observeOverdueTaskCount(): Flow<Int>
}
