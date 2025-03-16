package com.eysamarin.squadplay.domain.auth

import com.eysamarin.squadplay.contracts.AuthRepository

interface AuthProvider {
    suspend fun signInWithGoogle(): Boolean
    suspend fun signOut(): Boolean
    fun isUserSigned(): Boolean
}

class AuthProviderImpl(
    private val authRepository: AuthRepository,
) : AuthProvider {

    override fun isUserSigned(): Boolean = authRepository.isUserSigned()

    override suspend fun signInWithGoogle() = authRepository.signInWithGoogle()
    override suspend fun signOut() = authRepository.signOut()
}
