package com.eysamarin.squadplay.screens.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.RegistrationScreenUI
import com.eysamarin.squadplay.models.Route
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistrationScreenViewModel(
    private val authProvider: AuthProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<RegistrationScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.emit(UiState.Normal(RegistrationScreenUI(title = "Registration")))
        }
    }

    fun onConfirmTap(email: String, password: String) = viewModelScope.launch {
        Log.d("TAG", "onConfirmTap")
        val signUpState = authProvider.signUpWithEmailPassword(email, password)

        when (signUpState) {
            UiState.Empty,
            UiState.Loading -> Unit

            is UiState.Error -> {
                Log.w("TAG", signUpState.description)
                snackbarChannel.send(signUpState.description)
            }

            is UiState.Normal<*> -> navigationChannel.send(NavAction.NavigateTo(Route.Auth.route))
        }
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigationChannel.send(NavAction.NavigateBack)
    }
}