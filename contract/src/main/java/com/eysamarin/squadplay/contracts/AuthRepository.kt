package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User

interface AuthRepository {
    suspend fun signInWithGoogle(): User?
    suspend fun signUpWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signInWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signOut(): Boolean
    fun getCurrentUserId(): String?

    @Throws(AppErrorException::class)
    suspend fun isUserExists(): Boolean
}