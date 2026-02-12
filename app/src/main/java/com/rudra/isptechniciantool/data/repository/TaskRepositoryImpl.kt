package com.rudra.isptechniciantool.data.repository

import com.rudra.isptechniciantool.data.local.dao.TaskDao
import com.rudra.isptechniciantool.data.local.entity.TaskEntity
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskStatus
import com.rudra.isptechniciantool.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository using Room database.
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override suspend fun createTask(task: Task): Long {
        return taskDao.insert(TaskEntity.fromDomainModel(task))
    }
    
    override suspend fun updateTask(task: Task) {
        taskDao.update(TaskEntity.fromDomainModel(task))
    }
    
    override suspend fun deleteTask(task: Task) {
        taskDao.delete(TaskEntity.fromDomainModel(task))
    }
    
    override suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteById(taskId)
    }
    
    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getById(taskId)?.toDomainModel()
    }
    
    override fun observeTaskById(taskId: Long): Flow<Task?> {
        return taskDao.observeById(taskId).map { it?.toDomainModel() }
    }
    
    override fun observeAllTasks(): Flow<List<Task>> {
        return taskDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.observeByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observePendingTasks(): Flow<List<Task>> {
        return taskDao.observePendingTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeOverdueTasks(): Flow<List<Task>> {
        return taskDao.observeOverdueTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun observeTasksByCustomerId(customerId: Long): Flow<List<Task>> {
        return taskDao.observeByCustomerId(customerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun updateTaskStatus(taskId: Long, status: TaskStatus) {
        taskDao.updateStatus(taskId, status.name)
    }
    
    override suspend fun markTaskCompleted(taskId: Long) {
        taskDao.markCompleted(taskId)
    }
    
    override fun observePendingTaskCount(): Flow<Int> {
        return taskDao.observePendingCount()
    }
    
    override fun observeOverdueTaskCount(): Flow<Int> {
        return taskDao.observeOverdueCount()
    }
}
