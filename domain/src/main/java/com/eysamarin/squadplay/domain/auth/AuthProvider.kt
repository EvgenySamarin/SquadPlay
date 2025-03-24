package com.eysamarin.squadplay.domain.auth

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.models.User

interface AuthProvider {
    suspend fun signInWithGoogle(): Boolean
    suspend fun signInWithEmailPassword(email: String, password: String): Boolean
    suspend fun signOut(): Boolean
    fun isUserSigned(): Boolean
}

class AuthProviderImpl(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : AuthProvider {

    override fun isUserSigned(): Boolean = authRepository.isUserSigned()

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String,
    ): Boolean = authRepository.signInWithEmailPassword(email, password)
        ?.handleSignIn() == true

    override suspend fun signInWithGoogle(): Boolean = authRepository.signInWithGoogle()
            ?.handleSignIn() == true

    private suspend fun User.handleSignIn(): Boolean {
        profileRepository.saveUserProfile(this)
        return true
    }

    override suspend fun signOut(): Boolean {
        val currentUserId = authRepository.getCurrentUserId()
        profileRepository.deleteUserProfile(currentUserId)
        val isSignOutSuccess = authRepository.signOut()

        return isSignOutSuccess
    }
}
