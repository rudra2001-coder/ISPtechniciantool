package com.rudra.isptechniciantool.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.SyncLog
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.SyncLogRepository
import com.rudra.isptechniciantool.domain.repository.TaskRepository
import com.rudra.isptechniciantool.domain.usecase.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val taskRepository: TaskRepository,
    private val routerRepository: RouterRepository,
    private val syncLogRepository: SyncLogRepository
) : ViewModel() {

    private val syncEngine = SyncEngine(routerRepository, customerRepository, syncLogRepository)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                customerRepository.observeTotalCustomerCount(),
                customerRepository.observeActiveCustomerCount(),
                customerRepository.observePendingSyncCount(),
                customerRepository.observeFailedSyncCount(),
                customerRepository.observeRecentlyModifiedCustomers(5),
                taskRepository.observePendingTasks(),
                taskRepository.observeOverdueTasks(),
                routerRepository.observeDefaultEnabledRouter(),
                syncLogRepository.observeRecentSyncLogs(1)
            ) { values ->
                val totalCustomers = values[0] as Int
                val activeCustomers = values[1] as Int
                val pendingSync = values[2] as Int
                val failedSync = values[3] as Int
                @Suppress("UNCHECKED_CAST")
                val recentCustomers = values[4] as List<Customer>
                @Suppress("UNCHECKED_CAST")
                val pendingTasks = values[5] as List<Task>
                @Suppress("UNCHECKED_CAST")
                val overdueTasks = values[6] as List<Task>
                @Suppress("UNCHECKED_CAST")
                val router = values[7] as? com.rudra.isptechniciantool.domain.model.Router
                @Suppress("UNCHECKED_CAST")
                val syncLogs = values[8] as List<SyncLog>
                val lastSyncTime = syncLogs.firstOrNull()?.startTime

                DashboardUiState(
                    totalCustomers = totalCustomers,
                    activeCustomers = activeCustomers,
                    pendingSyncCount = pendingSync,
                    failedSyncCount = failedSync,
                    recentCustomers = recentCustomers,
                    pendingTasks = pendingTasks.take(5),
                    overdueTaskCount = overdueTasks.size,
                    isLoading = false,
                    isRouterConfigured = router != null,
                    routerName = router?.name,
                    isSyncing = false,
                    lastSyncTime = lastSyncTime
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboardData()
    }

    fun syncNow() {
        val currentState = _uiState.value
        if (currentState.pendingSyncCount == 0) {
            _uiState.value = currentState.copy(
                syncMessage = "No pending customers to sync"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncMessage = null,
                error = null
            )

            val result = syncEngine.syncAllPendingCustomers()

            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncMessage = if (result.success) {
                    "Sync completed: ${result.successCount} succeeded, ${result.failedCount} failed"
                } else {
                    result.errorMessage
                },
                error = if (!result.success) result.errorMessage else null
            )

            // Refresh data after sync
            loadDashboardData()
        }
    }

    fun clearSyncMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null)
    }
}

/**
 * UI state for the Dashboard screen.
 */
data class DashboardUiState(
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val pendingSyncCount: Int = 0,
    val failedSyncCount: Int = 0,
    val recentCustomers: List<Customer> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val overdueTaskCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isRouterConfigured: Boolean = false,
    val routerName: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val lastSyncTime: LocalDateTime? = null
)
