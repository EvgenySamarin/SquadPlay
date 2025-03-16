package com.eysamarin.squadplay.contracts

interface AuthRepository {
    suspend fun signInWithGoogle(): Boolean
    suspend fun signOut(): Boolean
    fun getCurrentUserId(): String
    fun isUserSigned(): Boolean
}