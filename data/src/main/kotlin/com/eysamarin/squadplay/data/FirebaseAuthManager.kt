package com.eysamarin.squadplay.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

interface FirebaseAuthManager {
    fun authWithGoogle(idToken: String)
    fun isUserSigned(): Boolean
}

class FirebaseAuthManagerImpl(
    private val firebaseAuth: FirebaseAuth,
    private val appContext: Context,
) : FirebaseAuthManager {

    override fun isUserSigned(): Boolean {
        val currentUser = firebaseAuth.currentUser
        Log.d("TAG", "currentUser: $currentUser")
        return currentUser != null
    }

    override fun authWithGoogle(idToken: String) {
        Log.d("TAG", "authWithGoogle with token $idToken on context $appContext")
    }
}