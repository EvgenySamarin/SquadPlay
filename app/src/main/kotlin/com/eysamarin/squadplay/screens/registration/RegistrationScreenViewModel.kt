package com.eysamarin.squadplay.screens.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.RegistrationScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class RegistrationScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val authProvider: AuthProvider,
) : ViewModel() {

    fun onConfirmTap(email: String, password: String) = viewModelScope.launch {
        Log.d("TAG", "onConfirmTap")
        val signUpState = authProvider.signUpWithEmailPassword(email, password)

        when (signUpState) {
            UiState.Empty,
            UiState.Loading -> Unit

            is UiState.Error -> {
                Log.w("TAG", signUpState.description)
                snackbar.showMessage(signUpState.description)
            }

            is UiState.Normal<*> -> navigator.navigate(Destination.AuthScreen)
        }
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onAction(action: RegistrationScreenAction) {
        when (action) {
            is RegistrationScreenAction.OnConfirmTap -> onConfirmTap(action.email, action.password)
            RegistrationScreenAction.OnBackButtonTap -> onBackButtonTap()
        }
    }
}