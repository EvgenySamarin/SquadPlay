package com.eysamarin.squadplay.domain.auth

import com.eysamarin.squadplay.contracts.AuthRepository

interface AuthProvider {
    fun authWithGoogle(idToken: String)
    fun isUserSigned(): Boolean
}

class AuthProviderImpl(
    private val authRepository: AuthRepository,
) : AuthProvider {

    override fun isUserSigned(): Boolean = authRepository.isUserSigned()

    override fun authWithGoogle(idToken: String) {
        authRepository.authWithGoogle(idToken)
    }
}
