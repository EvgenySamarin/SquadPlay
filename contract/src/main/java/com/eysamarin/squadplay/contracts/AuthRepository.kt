package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User

interface AuthRepository {
    suspend fun signInWithGoogle(): User?
    suspend fun signUpWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signInWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signOut(): Boolean
    fun getCurrentUserId(): String
    suspend fun isUserExists(): Boolean
}