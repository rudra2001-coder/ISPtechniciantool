package com.rudra.isptechniciantool.ui.screens.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.RouterRepository

/**
 * Factory for creating RouterSettingsViewModel
 */
class RouterSettingsViewModelFactory(
    private val routerRepository: RouterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RouterSettingsViewModel::class.java)) {
            return RouterSettingsViewModel(routerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
