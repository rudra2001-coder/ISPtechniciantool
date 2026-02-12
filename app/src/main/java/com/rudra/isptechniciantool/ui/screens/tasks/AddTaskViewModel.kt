package com.rudra.isptechniciantool.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskPriority
import com.rudra.isptechniciantool.domain.model.TaskStatus
import com.rudra.isptechniciantool.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: Long? = null,
    val isLoading: Boolean = false,
    val titleError: String? = null
)

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()
    
    private var taskId: Long? = null
    
    fun loadTask(id: Long) {
        taskId = id
        viewModelScope.launch {
            taskRepository.getTaskById(id)?.let { task ->
                _uiState.update {
                    it.copy(
                        title = task.title,
                        description = task.description ?: "",
                        priority = task.priority,
                        status = task.status,
                        dueDate = task.dueDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                    )
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updatePriority(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }
    
    fun updateStatus(status: TaskStatus) {
        _uiState.update { it.copy(status = status) }
    }
    
    fun updateDueDate(millis: Long?) {
        _uiState.update { it.copy(dueDate = millis) }
    }
    
    fun saveTask() {
        val state = _uiState.value
        
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val dueDateTime = state.dueDate?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            }
            
            val task = Task(
                id = taskId ?: 0,
                title = state.title,
                description = state.description.ifBlank { null },
                priority = state.priority,
                status = state.status,
                dueDate = dueDateTime,
                modifiedAt = LocalDateTime.now()
            )
            
            if (taskId != null) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.createTask(task)
            }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
