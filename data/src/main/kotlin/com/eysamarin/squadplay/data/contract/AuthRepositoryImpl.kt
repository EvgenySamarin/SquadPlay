package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User

class AuthRepositoryImpl(
    val firebaseAuthManager: FirebaseAuthManager,
) : AuthRepository {

    override fun getCurrentUserId(): String = firebaseAuthManager.getCurrentUserId()
    override fun isUserSigned(): Boolean = firebaseAuthManager.isUserSigned()
    override suspend fun signInWithGoogle() = firebaseAuthManager.signInWithGoogle()

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String,
    ): User? = firebaseAuthManager.signUpWithEmailPassword(email, password)

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String,
    ): UiState<User> = firebaseAuthManager.signInWithEmailPassword(email, password)

    override suspend fun signOut() = firebaseAuthManager.signOut()
}