package com.eysamarin.squadplay

import com.eysamarin.squadplay.contracts.SecurityLockoutManager
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.models.AppError
import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.DefaultDeepLinkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchApplicationViewModelSecurityTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthProvider : AuthProvider {
        var shouldThrowAppCheckError = false
        var isUserExistsResult = false

        override suspend fun isUserExists(): Boolean {
            if (shouldThrowAppCheckError) {
                throw AppErrorException(AppError.SecurityAttestationFailed)
            }
            return isUserExistsResult
        }

        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Empty
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Empty
        override suspend fun signOut(): Boolean = true
        override fun getCurrentUserIdFlow(): Flow<String?> = flowOf("test_uid")
    }

    private class FakeSecurityLockoutManager : SecurityLockoutManager {
        override val isLockedOut: StateFlow<Boolean>
            field = MutableStateFlow(false)

        var retryCallCount = 0

        override fun triggerLockout() {
            isLockedOut.value = true
        }

        override suspend fun retryAttestation(): Result<Unit> {
            retryCallCount++
            isLockedOut.value = false
            return Result.success(Unit)
        }

        override fun clearLockout() {
            isLockedOut.value = false
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isSecurityLockedOut reflects SecurityLockoutManager state`() = runTest {
        val lockoutManager = FakeSecurityLockoutManager()
        val viewModel = LaunchApplicationViewModel(
            authProvider = FakeAuthProvider(),
            deepLinkManager = DefaultDeepLinkManager(),
            securityLockoutManager = lockoutManager,
        )

        assertFalse(viewModel.isSecurityLockedOut.value)
        lockoutManager.triggerLockout()
        assertTrue(viewModel.isSecurityLockedOut.value)
    }

    @Test
    fun `retrySecurityAttestation triggers retry on SecurityLockoutManager`() = runTest {
        val lockoutManager = FakeSecurityLockoutManager()
        lockoutManager.triggerLockout()

        val viewModel = LaunchApplicationViewModel(
            authProvider = FakeAuthProvider(),
            deepLinkManager = DefaultDeepLinkManager(),
            securityLockoutManager = lockoutManager,
        )

        assertTrue(viewModel.isSecurityLockedOut.value)

        viewModel.retrySecurityAttestation()
        advanceUntilIdle()

        assertEquals(1, lockoutManager.retryCallCount)
        assertFalse(viewModel.isSecurityLockedOut.value)
    }

    @Test
    fun `handleIncomingIntent triggers lockout when auth check encounters attestation failure`() = runTest {
        val fakeAuth = FakeAuthProvider().apply { shouldThrowAppCheckError = true }
        val lockoutManager = FakeSecurityLockoutManager()

        val viewModel = LaunchApplicationViewModel(
            authProvider = fakeAuth,
            deepLinkManager = DefaultDeepLinkManager(),
            securityLockoutManager = lockoutManager,
        )

        viewModel.handleIncomingIntent(null)
        advanceUntilIdle()

        assertTrue(viewModel.isSecurityLockedOut.value)
    }
}
