package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.AuthRepository
import com.eysamarin.squadplay.data.FirebaseAuthManager

class AuthRepositoryImpl(
    val firebaseAuthManager: FirebaseAuthManager,
) : AuthRepository {

    override fun isUserSigned(): Boolean = firebaseAuthManager.isUserSigned()

    override fun authWithGoogle(idToken: String) {
        firebaseAuthManager.authWithGoogle(idToken)
    }
}