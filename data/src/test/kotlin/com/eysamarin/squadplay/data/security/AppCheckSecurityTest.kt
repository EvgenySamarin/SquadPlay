package com.eysamarin.squadplay.data.security

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.models.AppError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCheckSecurityTest {

    private class RecordingLogger : AppLogger {
        val errorLogs = mutableListOf<String>()
        val warningLogs = mutableListOf<String>()
        val debugLogs = mutableListOf<String>()
        val infoLogs = mutableListOf<String>()

        override fun d(tag: String?, message: () -> String) {
            debugLogs.add(message())
        }

        override fun i(tag: String?, message: () -> String) {
            infoLogs.add(message())
        }

        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {
            warningLogs.add(message())
        }

        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
            errorLogs.add(message())
        }
    }

    @Test
    fun `isAppCheckAttestationFailure matches attestation failed message`() {
        val exception = RuntimeException("Firebase error: App attestation failed")
        assertTrue(exception.isAppCheckAttestationFailure())
        assertEquals(AppError.SecurityAttestationFailed, exception.toAppError())
    }

    @Test
    fun `isAppCheckAttestationFailure matches 403 and App Check message`() {
        val exception = RuntimeException("HTTP 403: App Check token is invalid or missing")
        assertTrue(exception.isAppCheckAttestationFailure())
        assertEquals(AppError.SecurityAttestationFailed, exception.toAppError())
    }

    @Test
    fun `isAppCheckAttestationFailure returns false for standard network errors`() {
        val exception = RuntimeException("Network connection failed")
        assertFalse(exception.isAppCheckAttestationFailure())
        assertNull(exception.toAppError())
    }

    @Test
    fun `isAppCheckAttestationFailure matches AppErrorException`() {
        val exception = com.eysamarin.squadplay.models.AppErrorException(AppError.SecurityAttestationFailed)
        assertTrue(exception.isAppCheckAttestationFailure())
        assertEquals(AppError.SecurityAttestationFailed, exception.toAppError())
    }

    @Test
    fun `isAppCheckAttestationFailure matches wrapped cause`() {
        val rootCause = RuntimeException("App attestation failed")
        val wrappedException = IllegalStateException("Operation failed", rootCause)
        assertTrue(wrappedException.isAppCheckAttestationFailure())
        assertEquals(AppError.SecurityAttestationFailed, wrappedException.toAppError())
    }

    @Test
    fun `SecurityLockoutManager updates state and logs on trigger and clear`() = runTest {
        val logger = RecordingLogger()
        val lockoutManager = SecurityLockoutManagerImpl(
            logger = logger,
        )

        assertFalse(lockoutManager.isLockedOut.value)

        lockoutManager.triggerLockout()
        assertTrue(lockoutManager.isLockedOut.value)
        assertEquals(1, logger.warningLogs.size)
        assertTrue(logger.warningLogs.first().contains("App Check attestation failure detected"))

        lockoutManager.clearLockout()
        assertFalse(lockoutManager.isLockedOut.value)
        assertEquals(1, logger.infoLogs.size)
        assertTrue(logger.infoLogs.first().contains("Clearing security lockout"))
    }
}
