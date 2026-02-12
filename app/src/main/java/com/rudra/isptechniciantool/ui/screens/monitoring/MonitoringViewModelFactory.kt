package com.rudra.isptechniciantool.ui.screens.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import com.rudra.isptechniciantool.domain.repository.LinkRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating MonitoringViewModel with dependencies.
 * Uses Hilt for dependency injection.
 */
@Singleton
class MonitoringViewModelFactory @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val linkRepository: LinkRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitoringViewModel::class.java)) {
            return MonitoringViewModel(
                deviceRepository = deviceRepository,
                linkRepository = linkRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
