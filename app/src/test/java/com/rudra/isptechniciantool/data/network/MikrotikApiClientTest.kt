package com.rudra.isptechniciantool.data.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

/**
 * Unit tests for MikrotikApiClient
 * Note: These tests focus on state management and basic logic.
 * Full socket testing would require integration tests.
 */
class MikrotikApiClientTest {

    private lateinit var apiClient: MikrotikApiClient

    @Before
    fun setup() {
        apiClient = MikrotikApiClient()
    }

    @Test
    fun `isConnected returns false initially`() {
        assertFalse(apiClient.isConnected())
    }

    @Test
    fun `disconnect sets connection state to false`() {
        // After disconnect, connection state should be false
        apiClient.disconnect()
        assertFalse(apiClient.isConnected())
    }

    @Test
    fun `connect with invalid port returns failure`() = runTest {
        // Given - using an invalid port
        val result = apiClient.connect("192.168.1.1", -1)

        // Then - should fail
        assertTrue(result.isFailure)
    }

    @Test
    fun `createPppSecret returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.createPppSecret(
            username = "testuser",
            password = "testpass",
            profile = "default",
            remoteAddress = "10.0.0.1"
        )

        // Then - should fail with connection error
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Not connected") == true)
    }

    @Test
    fun `getAllSecrets returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.getAllSecrets()

        // Then - should fail with connection error
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Not connected") == true)
    }

    @Test
    fun `enableSecret returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.enableSecret("testuser")

        // Then - should fail with connection error
        assertTrue(result.isFailure)
    }

    @Test
    fun `disableSecret returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.disableSecret("testuser")

        // Then - should fail with connection error
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteSecret returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.deleteSecret("testuser")

        // Then - should fail with connection error
        assertTrue(result.isFailure)
    }

    @Test
    fun `executeCommand returns failure when not connected`() = runTest {
        // Given - not connected
        val result = apiClient.executeCommand("/system/resource/print")

        // Then - should fail with connection error
        assertTrue(result.isFailure)
    }
}

/**
 * Tests for custom exceptions
 */
class MikrotikExceptionsTest {

    @Test
    fun `ConnectionException contains correct message`() {
        val exception = ConnectionException("Test connection error")
        assertEquals("Test connection error", exception.message)
    }

    @Test
    fun `ConnectionException contains cause`() {
        val cause = Exception("Original cause")
        val exception = ConnectionException("Test error", cause)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `AuthenticationException contains correct message`() {
        val exception = AuthenticationException("Login failed")
        assertEquals("Login failed", exception.message)
    }

    @Test
    fun `ApiException contains correct message`() {
        val exception = ApiException("API command failed")
        assertEquals("API command failed", exception.message)
    }
}

/**
 * Tests for PppSecret data class
 */
class PppSecretTest {

    @Test
    fun `PppSecret stores all fields correctly`() {
        val secret = PppSecret(
            id = "*1",
            name = "testuser",
            password = "secret123",
            profile = "default",
            remoteAddress = "10.0.0.1",
            disabled = false
        )

        assertEquals("*1", secret.id)
        assertEquals("testuser", secret.name)
        assertEquals("secret123", secret.password)
        assertEquals("default", secret.profile)
        assertEquals("10.0.0.1", secret.remoteAddress)
        assertFalse(secret.disabled)
    }

    @Test
    fun `PppSecret disabled state is correct`() {
        val disabledSecret = PppSecret(
            id = "*1",
            name = "disableduser",
            password = "pass",
            profile = "default",
            remoteAddress = null,
            disabled = true
        )

        assertTrue(disabledSecret.disabled)
        assertNull(disabledSecret.remoteAddress)
    }
}
