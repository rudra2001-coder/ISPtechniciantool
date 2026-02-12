package com.rudra.isptechniciantool.ui.screens.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for Add/Edit Customer screen.
 */
@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L
    val isEditMode: Boolean = customerId > 0
    
    private val _uiState = MutableStateFlow(AddCustomerUiState())
    val uiState: StateFlow<AddCustomerUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<AddCustomerEvent>()
    val events = _events.asSharedFlow()
    
    init {
        if (isEditMode) {
            loadCustomer(customerId)
        }
    }
    
    private fun loadCustomer(id: Long) {
        viewModelScope.launch {
            customerRepository.getCustomerById(id)?.let { customer ->
                _uiState.update {
                    it.copy(
                        name = customer.name,
                        phone = customer.phone,
                        phoneSecondary = customer.phoneSecondary ?: "",
                        email = customer.email ?: "",
                        address = customer.address,
                        addressNotes = customer.addressNotes ?: "",
                        username = customer.username,
                        password = customer.password,
                        ipAddress = customer.ipAddress ?: "",
                        packageName = customer.packageName ?: "",
                        customerStatus = customer.customerStatus,
                        notes = customer.notes ?: "",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }
    
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }
    
    fun updatePhoneSecondary(phone: String) {
        _uiState.update { it.copy(phoneSecondary = phone) }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }
    
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
    }
    
    fun updateAddressNotes(notes: String) {
        _uiState.update { it.copy(addressNotes = notes) }
    }
    
    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, usernameError = null) }
    }
    
    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }
    
    fun updateIpAddress(ip: String) {
        _uiState.update { it.copy(ipAddress = ip) }
    }
    
    fun updatePackageName(packageName: String) {
        _uiState.update { it.copy(packageName = packageName) }
    }
    
    fun updateCustomerStatus(status: CustomerStatus) {
        _uiState.update { it.copy(customerStatus = status) }
    }
    
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    fun saveCustomer() {
        val state = _uiState.value
        
        // Validation
        var hasError = false
        
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone is required") }
            hasError = true
        }
        
        if (state.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            hasError = true
        }
        
        if (state.address.isBlank()) {
            _uiState.update { it.copy(addressError = "Address is required") }
            hasError = true
        }
        
        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username is required") }
            hasError = true
        }
        
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }
        
        if (hasError) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val customer = Customer(
                    id = if (isEditMode) customerId else 0,
                    name = state.name.trim(),
                    phone = state.phone.trim(),
                    phoneSecondary = state.phoneSecondary.takeIf { it.isNotBlank() },
                    email = state.email.takeIf { it.isNotBlank() },
                    address = state.address.trim(),
                    addressNotes = state.addressNotes.takeIf { it.isNotBlank() },
                    username = state.username.trim(),
                    password = state.password,
                    ipAddress = state.ipAddress.takeIf { it.isNotBlank() },
                    packageName = state.packageName.takeIf { it.isNotBlank() },
                    customerStatus = state.customerStatus,
                    notes = state.notes.takeIf { it.isNotBlank() },
                    modifiedAt = LocalDateTime.now()
                )
                
                if (isEditMode) {
                    customerRepository.updateCustomer(customer)
                } else {
                    customerRepository.createCustomer(customer)
                }
                
                _events.emit(AddCustomerEvent.CustomerSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(AddCustomerEvent.Error(e.message ?: "Failed to save customer"))
            }
        }
    }
}

/**
 * UI state for Add Customer screen.
 */
data class AddCustomerUiState(
    val name: String = "",
    val phone: String = "",
    val phoneSecondary: String = "",
    val email: String = "",
    val address: String = "",
    val addressNotes: String = "",
    val username: String = "",
    val password: String = "",
    val ipAddress: String = "",
    val packageName: String = "",
    val customerStatus: CustomerStatus = CustomerStatus.PENDING,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val addressError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val error: String? = null
)

/**
 * Events emitted by AddCustomerViewModel.
 */
sealed class AddCustomerEvent {
    object CustomerSaved : AddCustomerEvent()
    data class Error(val message: String) : AddCustomerEvent()
}
