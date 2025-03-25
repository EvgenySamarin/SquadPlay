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
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await

interface FirebaseAuthManager {
    suspend fun signInWithGoogle(): User?
    suspend fun signInWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signUpWithEmailPassword(email: String, password: String): UiState<User>
    suspend fun signOut(): Boolean
    fun getUserUid(): String?
    fun getCurrentUserId(): String
}

class FirebaseAuthManagerImpl(
    private val firebaseAuth: FirebaseAuth,
    private val webClientId: String,
    private val credentialManager: CredentialManager,
    private val appContext: Context,
) : FirebaseAuthManager {

    override fun getUserUid(): String? {
        val currentUser = firebaseAuth.currentUser
        Log.d("TAG", "currentUser: $currentUser")
        return currentUser?.uid
    }

    override fun getCurrentUserId(): String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User is not signed in")

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): UiState<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            val firebaseUser = result.user
            if (firebaseUser == null) return UiState.Error("User does not exist")

            Log.d("TAG", "signUpWithEmailPassword:success")

            UiState.Normal(firebaseUser.toAppUser())
        } catch (exception: FirebaseAuthWeakPasswordException){
            Log.w("TAG", "signUpWithEmailPassword:failure", exception)
            UiState.Error(exception.message ?: "Password is too weak")
        }
        catch (exception: Exception) {
            Log.w("TAG", "signUpWithEmailPassword:failure", exception)
            UiState.Error("Unexpected exception occurred")
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): UiState<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            val firebaseUser = result.user
            if (firebaseUser == null) return UiState.Error("User does not exist")

            Log.d("TAG", "signInWithEmailPassword:success")

            UiState.Normal(firebaseUser.toAppUser())
        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            Log.w("TAG", "signInWithEmailPassword:failure", exception)
            UiState.Error("Cannot login, please check your email or password")
        } catch (exception: Exception) {
            Log.w("TAG", "signInWithEmailPassword:failure", exception)
            UiState.Error("Unexpected exception occurred")
        }
    }

    override suspend fun signInWithGoogle(): User? {
        val credential = getUserCredential(filterByAuthorizedAccounts = true)
            ?: getUserCredential(filterByAuthorizedAccounts = false)

        return if (credential != null) {
            handleSignIn(credential)
        } else {
            null
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

    private suspend fun handleSignIn(credential: Credential): User? {
        return if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w("TAG", "Credential is not of type Google ID!")
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun firebaseAuthWithGoogle(idToken: String): User? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = result.user
            if (firebaseUser == null) return null

            Log.d("TAG", "signInWithCredential:success")

            firebaseUser.toAppUser()
        } catch (e: Exception) {
            Log.w("TAG", "signInWithCredential:failure", e)
            null
        }
    }

    private fun FirebaseUser.toAppUser(): User {
        return User(
            uid = uid,
            username = displayName ?: "User",
            email = email,
            photoUrl = photoUrl?.toString(),
            groups = listOf(),
        )
    }

    override suspend fun signOut(): Boolean {
        firebaseAuth.signOut()
        return try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            Log.w("TAG", "User credentials cleared")
            true
        } catch (e: ClearCredentialException) {
            Log.e("TAG", "Couldn't clear user credentials: ${e.localizedMessage}")
            false
        }
    }
}