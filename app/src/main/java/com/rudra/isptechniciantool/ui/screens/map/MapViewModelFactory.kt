package com.rudra.isptechniciantool.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import javax.inject.Inject

/**
 * Factory for creating MapViewModel with dependencies.
 */
class MapViewModelFactory @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(deviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
