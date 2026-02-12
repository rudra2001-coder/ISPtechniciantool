package com.rudra.isptechniciantool.ui.screens.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.isptechniciantool.domain.repository.DeviceRepository
import com.rudra.isptechniciantool.domain.repository.LinkRepository

/**
 * Factory for creating ExportViewModel with dependencies.
 */
class ExportViewModelFactory(
    private val deviceRepository: DeviceRepository,
    private val linkRepository: LinkRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportViewModel::class.java)) {
            return ExportViewModel(
                deviceRepository = deviceRepository,
                linkRepository = linkRepository,
                context = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
