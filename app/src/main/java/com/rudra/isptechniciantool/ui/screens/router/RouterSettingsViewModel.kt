package com.rudra.isptechniciantool.ui.screens.router


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.rudra.isptechniciantool.data.network.AuthenticationException
import com.rudra.isptechniciantool.data.network.ConnectionException
import com.rudra.isptechniciantool.data.network.MikrotikApiClient
import com.rudra.isptechniciantool.domain.model.Router
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Router Settings Screen
 */
data class RouterSettingsUiState(
    val host: String = "",
    val port: String = "8728",
    val username: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val isTesting: Boolean = false,
    val testSuccess: Boolean? = null,
    val testMessage: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val existingRouter: Router? = null
)

/**
 * ViewModel for Router Settings Screen
 */
@HiltViewModel
class RouterSettingsViewModel @Inject constructor(
    private val routerRepository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouterSettingsUiState())
    val uiState: StateFlow<RouterSettingsUiState> = _uiState.asStateFlow()

    private val mikrotikApiClient = MikrotikApiClient()

    init {
        loadExistingRouter()
    }

    private fun loadExistingRouter() {
        viewModelScope.launch {
            val router = routerRepository.getDefaultEnabledRouter()
            router?.let {
                _uiState.update { state ->
                    state.copy(
                        host = it.host,
                        port = it.port.toString(),
                        username = it.username,
                        password = it.password,
                        name = it.name,
                        existingRouter = it
                    )
                }
            }
        }
    }

    fun updateHost(host: String) {
        _uiState.update { it.copy(host = host, testSuccess = null) }
    }

    fun updatePort(port: String) {
        _uiState.update { it.copy(port = port, testSuccess = null) }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, testSuccess = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, testSuccess = null) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    /**
     * Test connection to the router
     */
    fun testConnection() {
        val state = _uiState.value
        
        if (state.host.isBlank() || state.port.isBlank() || 
            state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, errorMessage = null, testSuccess = null) }

            try {
                val port = state.port.toIntOrNull() ?: 8728
                
                // Connect to router
                val connectResult = mikrotikApiClient.connect(state.host, port)
                
                if (connectResult.isFailure) {
                    mikrotikApiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isTesting = false,
                            testSuccess = false,
                            testMessage = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
                
                // Login to router
                val loginResult = mikrotikApiClient.login(state.username, state.password)
                
                if (loginResult.isFailure) {
                    mikrotikApiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isTesting = false,
                            testSuccess = false,
                            testMessage = "Login failed: ${loginResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
                
                // Test successful
                mikrotikApiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isTesting = false,
                        testSuccess = true,
                        testMessage = "Connection successful!"
                    )
                }
            } catch (e: Exception) {
                mikrotikApiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isTesting = false,
                        testSuccess = false,
                        testMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Save router configuration
     */
    fun saveRouter() {
        val state = _uiState.value
        
        if (state.host.isBlank() || state.port.isBlank() || 
            state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val router = Router(
                    id = state.existingRouter?.id ?: 0,
                    name = state.name.ifBlank { "MikroTik Router" },
                    host = state.host,
                    port = state.port.toIntOrNull() ?: 8728,
                    username = state.username,
                    password = state.password
                )

                if (state.existingRouter != null) {
                    routerRepository.updateRouter(router)
                } else {
                    routerRepository.createRouter(router)
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSaved = true,
                        existingRouter = router
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        mikrotikApiClient.disconnect()
    }
}
