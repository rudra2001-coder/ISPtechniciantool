package com.rudra.isptechniciantool.ui.screens.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Customer Detail screen.
 */
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L
    
    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState: StateFlow<CustomerDetailUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<CustomerDetailEvent>()
    val events = _events.asSharedFlow()
    
    init {
        loadCustomer()
    }
    
    private fun loadCustomer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            customerRepository.getCustomerById(customerId)?.let { customer ->
                _uiState.update {
                    it.copy(
                        customer = customer,
                        isLoading = false
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Customer not found") }
            }
        }
    }
    
    fun deleteCustomer() {
        viewModelScope.launch {
            _uiState.value.customer?.let { customer ->
                customerRepository.deleteCustomer(customer)
                _events.emit(CustomerDetailEvent.Deleted)
            }
        }
    }
    
    fun retrySync() {
        viewModelScope.launch {
            _uiState.value.customer?.let { customer ->
                customerRepository.updateSyncStatus(customer.id, com.rudra.isptechniciantool.domain.model.SyncStatus.PENDING, null)
                _events.emit(CustomerDetailEvent.SyncRetried)
            }
        }
    }
}

/**
 * UI state for Customer Detail screen.
 */
data class CustomerDetailUiState(
    val customer: Customer? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Events emitted by CustomerDetailViewModel.
 */
sealed class CustomerDetailEvent {
    object Deleted : CustomerDetailEvent()
    object SyncRetried : CustomerDetailEvent()
}
