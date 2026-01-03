package com.eysamarin.squadplay.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthScreenViewModel(
    private val navigator: Navigator,
    private val authProvider: AuthProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    init {
        checkUserSignedIn()
    }

    private fun checkUserSignedIn() = viewModelScope.launch {
        val isUserExists = authProvider.isUserExists()

        if (isUserExists) {
            navigator.navigateToHomeGraph()
        }
    }

    fun onSignInWithGoogleTap() = viewModelScope.launch {
        Log.d("TAG", "onSignUpTap")
        val isSuccess = authProvider.signInWithGoogle()
        if (isSuccess) {
            navigator.navigateToHomeGraph()
        } else {
            snackbarChannel.send(stringProvider.cannotSignText)
            Log.d("TAG", "cannot sign in")
        }
    }

    fun onSignInTap(email: String, password: String) = viewModelScope.launch {
        Log.d("TAG", "onSignInTap: $email, $password")

        val signInState = authProvider.signInWithEmailPassword(email, password)

        when (signInState) {
            UiState.Empty,
            UiState.Loading -> Unit

            is UiState.Error -> {
                Log.w("TAG", signInState.description)
                snackbarChannel.send(signInState.description)
            }

            is UiState.Normal<*> -> navigator.navigateToHomeGraph()
        }
    }

    fun onSignUpTap() = viewModelScope.launch {
        Log.d("TAG", "onSignUpTap")
        navigator.navigate(Destination.RegistrationScreen)
    }
}