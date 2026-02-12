package com.rudra.isptechniciantool.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Customer List screen.
 */
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow<CustomerFilter>(CustomerFilter.All)
    val selectedFilter: StateFlow<CustomerFilter> = _selectedFilter.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        when {
            query.isNotBlank() -> customerRepository.searchCustomers(query)
            filter is CustomerFilter.BySyncStatus -> 
                customerRepository.observeBySyncStatus(filter.syncStatus)
            filter is CustomerFilter.ByCustomerStatus -> 
                customerRepository.observeByCustomerStatus(filter.customerStatus)
            else -> customerRepository.observeAllCustomers()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectFilter(filter: CustomerFilter) {
        _selectedFilter.value = filter
    }
    
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
        }
    }
}

/**
 * Filter options for customer list.
 */
sealed class CustomerFilter {
    object All : CustomerFilter()
    data class BySyncStatus(val syncStatus: SyncStatus) : CustomerFilter()
    data class ByCustomerStatus(val customerStatus: CustomerStatus) : CustomerFilter()
}
