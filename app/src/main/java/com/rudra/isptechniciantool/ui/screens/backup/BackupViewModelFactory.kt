package com.rudra.isptechniciantool.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.TaskRepository

/**
 * Factory for creating BackupViewModel
 */
class BackupViewModelFactory(
    private val customerRepository: CustomerRepository,
    private val routerRepository: RouterRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            return BackupViewModel(customerRepository, routerRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
