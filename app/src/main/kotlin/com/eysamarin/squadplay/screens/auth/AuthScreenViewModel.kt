package com.eysamarin.squadplay.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.models.AuthScreenUI
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.Route.Main
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthScreenViewModel(
    private val authProvider: AuthProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<AuthScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    init {
        checkUserSignedIn()
    }

    private fun checkUserSignedIn() = viewModelScope.launch {
        val isUserSigned = authProvider.isUserSigned()
        if (isUserSigned) {
            navigationChannel.send(NavAction.NavigateTo(Main.route))
        } else {
            _uiState.emit(UiState.Normal(AuthScreenUI(isSignButtonVisible = true)))
        }
    }

    fun onSignInWithGoogleTap() = viewModelScope.launch {
        Log.d("TAG", "onSignUpTap")
        val isSuccess = authProvider.signInWithGoogle()
        if (isSuccess) {
            navigationChannel.send(NavAction.NavigateTo(Main.route))
        } else {
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

            is UiState.Normal<*> -> navigationChannel.send(NavAction.NavigateTo(Main.route))
        }
    }
}