package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.contracts.ProfileRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User

class AuthRepositoryImpl(
    val firebaseAuthManager: FirebaseAuthManager,
    val profileRepository: ProfileRepository,
) : AuthRepository {

    override fun getCurrentUserId(): String = firebaseAuthManager.getCurrentUserId()
    override suspend fun isUserExists(): Boolean = firebaseAuthManager.getUserUid()?.let {
        profileRepository.isUserProfileExists(it)
    } == true

    override suspend fun signInWithGoogle() = firebaseAuthManager.signInWithGoogle()

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String,
    ): UiState<User> = firebaseAuthManager.signUpWithEmailPassword(email, password)

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String,
    ): UiState<User> = firebaseAuthManager.signInWithEmailPassword(email, password)

    override suspend fun signOut() = firebaseAuthManager.signOut()
}