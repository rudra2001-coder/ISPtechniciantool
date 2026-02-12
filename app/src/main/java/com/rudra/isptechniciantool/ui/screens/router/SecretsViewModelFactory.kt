package com.rudra.isptechniciantool.ui.screens.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.RouterRepository

/**
 * Factory for creating SecretsViewModel
 */
class SecretsViewModelFactory(
    private val routerRepository: RouterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecretsViewModel::class.java)) {
            return SecretsViewModel(routerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
