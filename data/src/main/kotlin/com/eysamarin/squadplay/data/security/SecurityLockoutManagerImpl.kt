package com.eysamarin.squadplay.data.security

import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.SecurityLockoutManager
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class SecurityLockoutManagerImpl(
    private val firebaseAppCheckProvider: () -> FirebaseAppCheck = { FirebaseAppCheck.getInstance() },
    private val logger: AppLogger,
) : SecurityLockoutManager {

    override val isLockedOut: StateFlow<Boolean>
        field = MutableStateFlow(false)

    override fun triggerLockout() {
        logger.w(tag = TAG) { "App Check attestation failure detected. Triggering security lockout." }
        isLockedOut.value = true
    }

    override suspend fun retryAttestation(): Result<Unit> = runCatching {
        logger.i(tag = TAG) { "Attempting App Check token refresh retry..." }
        val tokenResult = firebaseAppCheckProvider().getAppCheckToken(true).await()
        if (tokenResult.token.isNotBlank()) {
            logger.i(tag = TAG) { "App Check token refreshed successfully, clearing lockout." }
            isLockedOut.value = false
        } else {
            throw IllegalStateException("Empty App Check token received")
        }
    }.onFailure { error ->
        logger.e(tag = TAG, throwable = error) { "App Check token refresh failed on retry: ${error.message}" }
    }

    override fun clearLockout() {
        logger.i(tag = TAG) { "Clearing security lockout." }
        isLockedOut.value = false
    }

    companion object {
        private const val TAG = "SecurityLockout"
    }
}
