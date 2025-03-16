package com.eysamarin.squadplay.domain.auth

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository

interface AuthProvider {
    suspend fun signInWithGoogle(): Boolean
    suspend fun signOut(): Boolean
    fun isUserSigned(): Boolean
}

class AuthProviderImpl(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : AuthProvider {

    override fun isUserSigned(): Boolean = authRepository.isUserSigned()

    override suspend fun signInWithGoogle(): Boolean {
        val user = authRepository.signInWithGoogle()
        val isSignInSuccess = user != null

        if (isSignInSuccess) {
            profileRepository.saveUserProfile(user)
        }

        return isSignInSuccess
    }

    override suspend fun signOut(): Boolean {
        val currentUserId = authRepository.getCurrentUserId()
        profileRepository.deleteUserProfile(currentUserId)
        val isSignOutSuccess = authRepository.signOut()

        return isSignOutSuccess
    }
}
