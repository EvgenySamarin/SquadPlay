package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager

class AuthRepositoryImpl(
    val firebaseAuthManager: FirebaseAuthManager,
) : AuthRepository {

    override fun getCurrentUserId(): String = firebaseAuthManager.getCurrentUserId()
    override fun isUserSigned(): Boolean = firebaseAuthManager.isUserSigned()
    override suspend fun signInWithGoogle() = firebaseAuthManager.signInWithGoogle()
    override suspend fun signOut() = firebaseAuthManager.signOut()
}