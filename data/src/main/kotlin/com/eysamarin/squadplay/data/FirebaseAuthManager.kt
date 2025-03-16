package com.eysamarin.squadplay.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await

interface FirebaseAuthManager {
    suspend fun signInWithGoogle(): Boolean
    suspend fun signOut(): Boolean
    fun isUserSigned(): Boolean
    fun getCurrentUserId(): String
}

class FirebaseAuthManagerImpl(
    private val firebaseAuth: FirebaseAuth,
    private val webClientId: String,
    private val credentialManager: CredentialManager,
    private val appContext: Context,
) : FirebaseAuthManager {

    override fun isUserSigned(): Boolean {
        val currentUser = firebaseAuth.currentUser
        Log.d("TAG", "currentUser: $currentUser")
        return currentUser != null
    }

    override fun getCurrentUserId(): String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User is not signed in")

    override suspend fun signInWithGoogle(): Boolean {
        val credential = getUserCredential(filterByAuthorizedAccounts = true)
            ?: getUserCredential(filterByAuthorizedAccounts = false)

        return if (credential != null) {
            handleSignIn(credential)
        } else {
            false
        }
    }

    private suspend fun getUserCredential(filterByAuthorizedAccounts: Boolean): Credential? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            credentialManager
                .getCredential(context = appContext, request = request)
                .credential
        } catch (e: GetCredentialException) {
            Log.e(
                "TAG",
                "Couldn't retrieve user's credentials with authorized account filtered = $filterByAuthorizedAccounts: ${e.localizedMessage}"
            )
            null
        }
    }

    private suspend fun handleSignIn(credential: Credential): Boolean {
        return if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w("TAG", "Credential is not of type Google ID!")
            false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun firebaseAuthWithGoogle(idToken: String): Boolean {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            val user = firebaseAuth.currentUser
            Log.d("TAG", "signInWithCredential:success, user: $user")
            true
        } catch (e: Exception) {
            Log.w("TAG", "signInWithCredential:failure", e)
            false
        }
    }

    override suspend fun signOut(): Boolean {
        firebaseAuth.signOut()
        return try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            Log.e("TAG", "User credentials cleared")
            true
        } catch (e: ClearCredentialException) {
            Log.e("TAG", "Couldn't clear user credentials: ${e.localizedMessage}")
            false
        }
    }
}