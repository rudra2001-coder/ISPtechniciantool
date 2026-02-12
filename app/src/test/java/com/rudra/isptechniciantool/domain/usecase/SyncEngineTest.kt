package com.rudra.isptechniciantool.domain.usecase

import com.rudra.isptechniciantool.data.network.MikrotikApiClient
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.Router
import com.rudra.isptechniciantool.domain.model.SyncLog
import com.rudra.isptechniciantool.domain.model.SyncOperationType
import com.rudra.isptechniciantool.domain.model.SyncOutcome
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.SyncLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

/**
 * Unit tests for SyncEngine
 */
class SyncEngineTest {

    @Mock
    private lateinit var routerRepository: RouterRepository

    @Mock
    private lateinit var customerRepository: CustomerRepository

    @Mock
    private lateinit var syncLogRepository: SyncLogRepository

    @Mock
    private lateinit var apiClient: MikrotikApiClient

    private lateinit var syncEngine: SyncEngine

    private val testRouter = Router(
        id = 1,
        name = "Test Router",
        host = "192.168.1.1",
        port = 8728,
        username = "admin",
        password = "password"
    )

    private val testCustomer = Customer(
        id = 1,
        name = "John Doe",
        username = "johndoe",
        password = "secret123",
        phone = "1234567890",
        email = "john@example.com",
        packageName = "10Mbps",
        ipAddress = "10.0.0.50",
        address = "123 Main St",
        status = CustomerStatus.ACTIVE,
        syncStatus = com.rudra.isptechniciantool.domain.model.SyncStatus.PENDING
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        syncEngine = SyncEngine(routerRepository, customerRepository, syncLogRepository)
    }

    @Test
    fun `syncAllPendingCustomers returns error when no router configured`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(null)

        // When
        val result = syncEngine.syncAllPendingCustomers()

        // Then
        assertFalse(result.success)
        assertEquals(0, result.totalSynced)
        assertEquals("No router configured. Please configure router settings first.", result.errorMessage)
    }

    @Test
    fun `syncAllPendingCustomers returns error when connection fails`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(testRouter)
        whenever(apiClient.connect(any(), any())).thenReturn(Result.failure(Exception("Connection refused")))

        // When
        val result = syncEngine.syncAllPendingCustomers()

        // Then
        assertFalse(result.success)
        assertTrue(result.errorMessage?.contains("Failed to connect") == true)
    }

    @Test
    fun `syncAllPendingCustomers returns error when login fails`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(testRouter)
        whenever(apiClient.connect(any(), any())).thenReturn(Result.success(Unit))
        whenever(apiClient.login(any(), any())).thenReturn(Result.failure(Exception("Invalid credentials")))

        // When
        val result = syncEngine.syncAllPendingCustomers()

        // Then
        assertFalse(result.success)
        assertTrue(result.errorMessage?.contains("Login failed") == true)
    }

    @Test
    fun `syncCustomer returns success when customer synced successfully`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(testRouter)
        whenever(apiClient.connect(any(), any())).thenReturn(Result.success(Unit))
        whenever(apiClient.login(any(), any())).thenReturn(Result.success(true))
        whenever(apiClient.createPppSecret(any(), any(), any(), any())).thenReturn(Result.success(true))
        whenever(apiClient.disconnect()).then { }
        whenever(syncLogRepository.createSyncLog(any())).thenReturn(1L)

        // When
        val result = syncEngine.syncCustomer(testCustomer)

        // Then
        assertTrue(result.success)
        assertEquals(1, result.successCount)
        assertEquals(0, result.failedCount)
    }

    @Test
    fun `syncCustomer updates customer status to SYNCED on success`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(testRouter)
        whenever(apiClient.connect(any(), any())).thenReturn(Result.success(Unit))
        whenever(apiClient.login(any(), any())).thenReturn(Result.success(true))
        whenever(apiClient.createPppSecret(any(), any(), any(), any())).thenReturn(Result.success(true))
        whenever(apiClient.disconnect()).then { }
        whenever(syncLogRepository.createSyncLog(any())).thenReturn(1L)

        // When
        syncEngine.syncCustomer(testCustomer)

        // Then
        org.mockito.kotlin.verify(customerRepository).updateCustomer(
            org.mockito.kotlin.argThat { customer ->
                customer.status == CustomerStatus.SYNCED &&
                customer.lastSyncAt != null
            }
        )
    }

    @Test
    fun `syncCustomer updates customer status to FAILED on error`() = runTest {
        // Given
        whenever(routerRepository.getDefaultEnabledRouter()).thenReturn(testRouter)
        whenever(apiClient.connect(any(), any())).thenReturn(Result.success(Unit))
        whenever(apiClient.login(any(), any())).thenReturn(Result.success(true))
        whenever(apiClient.createPppSecret(any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("API error")))
        whenever(apiClient.disconnect()).then { }
        whenever(syncLogRepository.createSyncLog(any())).thenReturn(1L)

        // When
        val result = syncEngine.syncCustomer(testCustomer)

        // Then
        assertFalse(result.success)
        assertEquals(1, result.failedCount)
        org.mockito.kotlin.verify(customerRepository).updateCustomer(
            org.mockito.kotlin.argThat { customer ->
                customer.status == CustomerStatus.FAILED &&
                customer.syncError?.contains("API error") == true
            }
        )
    }
}

/**
 * Unit tests for SyncResult
 */
class SyncResultTest {

    @Test
    fun `SyncResult success is true when all syncs succeeded`() {
        val result = SyncResult(
            success = true,
            totalSynced = 5,
            successCount = 5,
            failedCount = 0,
            errorMessage = null
        )
        assertTrue(result.success)
        assertEquals(5, result.totalSynced)
        assertEquals(0, result.failedCount)
    }

    @Test
    fun `SyncResult success is false when any sync failed`() {
        val result = SyncResult(
            success = false,
            totalSynced = 5,
            successCount = 3,
            failedCount = 2,
            errorMessage = "2 customers failed to sync"
        )
        assertFalse(result.success)
        assertEquals(2, result.failedCount)
    }
}
