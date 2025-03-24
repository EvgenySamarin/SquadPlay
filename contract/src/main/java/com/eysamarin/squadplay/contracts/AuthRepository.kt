package com.eysamarin.squadplay.contracts

import com.eysamarin.squadplay.models.User

interface AuthRepository {
    suspend fun signInWithGoogle(): User?
    suspend fun signInWithEmailPassword(email: String, password: String): User?
    suspend fun signOut(): Boolean
    fun getCurrentUserId(): String
    fun isUserSigned(): Boolean
}