package com.rudra.isptechniciantool.ui.screens.router


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.rudra.isptechniciantool.data.network.MikrotikApiClient
import com.rudra.isptechniciantool.data.network.PppSecret
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Secrets Screen
 */
data class SecretsUiState(
    val secrets: List<PppSecret> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * ViewModel for Secrets Screen
 */
@HiltViewModel
class SecretsViewModel @Inject constructor(
    private val routerRepository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretsUiState())
    val uiState: StateFlow<SecretsUiState> = _uiState.asStateFlow()

    private val apiClient = MikrotikApiClient()

    init {
        loadSecrets()
    }

    /**
     * Load all PPP secrets from the router
     */
    fun loadSecrets() {
        viewModelScope.launch {
            val router = routerRepository.getDefaultEnabledRouter()
            
            if (router == null) {
                _uiState.update { 
                    it.copy(
                        error = "No router configured. Please configure router settings first.",
                        isLoading = false
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Connect to router
                val connectResult = apiClient.connect(router.host, router.port)
                if (connectResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                // Login to router
                val loginResult = apiClient.login(router.username, router.password)
                if (loginResult.isFailure) {
                    apiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Login failed: ${loginResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                // Get all secrets
                val secretsResult = apiClient.getAllSecrets()
                apiClient.disconnect()

                if (secretsResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to get secrets: ${secretsResult.exceptionOrNull()?.message}"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            secrets = secretsResult.getOrDefault(emptyList()),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                apiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Enable a PPP secret
     */
    fun enableSecret(secret: PppSecret) {
        viewModelScope.launch {
            val router = routerRepository.getDefaultEnabledRouter()
            
            if (router == null) {
                _uiState.update { it.copy(message = "No router configured") }
                return@launch
            }

            _uiState.update { 
                it.copy(isProcessing = true) 
            }

            try {
                val connectResult = apiClient.connect(router.host, router.port)
                if (connectResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val loginResult = apiClient.login(router.username, router.password)
                if (loginResult.isFailure) {
                    apiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Login failed: ${loginResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val enableResult = apiClient.enableSecret(secret.name)
                apiClient.disconnect()

                if (enableResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Secret '${secret.name}' enabled successfully"
                        )
                    }
                    loadSecrets()
                } else {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to enable: ${enableResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                apiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Disable a PPP secret
     */
    fun disableSecret(secret: PppSecret) {
        viewModelScope.launch {
            val router = routerRepository.getDefaultEnabledRouter()
            
            if (router == null) {
                _uiState.update { it.copy(message = "No router configured") }
                return@launch
            }

            _uiState.update { 
                it.copy(isProcessing = true) 
            }

            try {
                val connectResult = apiClient.connect(router.host, router.port)
                if (connectResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val loginResult = apiClient.login(router.username, router.password)
                if (loginResult.isFailure) {
                    apiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Login failed: ${loginResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val disableResult = apiClient.disableSecret(secret.name)
                apiClient.disconnect()

                if (disableResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Secret '${secret.name}' disabled successfully"
                        )
                    }
                    loadSecrets()
                } else {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to disable: ${disableResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                apiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Delete a PPP secret
     */
    fun deleteSecret(secret: PppSecret) {
        viewModelScope.launch {
            val router = routerRepository.getDefaultEnabledRouter()
            
            if (router == null) {
                _uiState.update { it.copy(message = "No router configured") }
                return@launch
            }

            _uiState.update { 
                it.copy(isProcessing = true) 
            }

            try {
                val connectResult = apiClient.connect(router.host, router.port)
                if (connectResult.isFailure) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to connect: ${connectResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val loginResult = apiClient.login(router.username, router.password)
                if (loginResult.isFailure) {
                    apiClient.disconnect()
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Login failed: ${loginResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val deleteResult = apiClient.deleteSecret(secret.name)
                apiClient.disconnect()

                if (deleteResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Secret '${secret.name}' deleted successfully"
                        )
                    }
                    loadSecrets()
                } else {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            message = "Failed to delete: ${deleteResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                apiClient.disconnect()
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onCleared() {
        super.onCleared()
        apiClient.disconnect()
    }
}
