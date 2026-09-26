package com.eysamarin.squadplay.contracts

import kotlinx.coroutines.flow.StateFlow

interface SecurityLockoutManager {
    val isLockedOut: StateFlow<Boolean>

    fun triggerLockout()

    suspend fun retryAttestation(): Result<Unit>

    fun clearLockout()
}
