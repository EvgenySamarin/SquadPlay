package com.eysamarin.squadplay.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class AuthScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val authProvider: AuthProvider,
    private val stringProvider: StringProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    fun onSignInWithGoogleTap() = viewModelScope.launch {
        analyticsProvider.trackEvent(AnalyticsEvent.SignInGoogleClicked)
        val isSuccess = authProvider.signInWithGoogle()
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.SignInSuccess)
            navigator.navigateToHomeGraph()
        } else {
            logger.w { "Failed to sign in with Google" }
            snackbar.showMessage(stringProvider.cannotSignText)
        }
    }

    fun onSignInTap(email: String, password: String) = viewModelScope.launch {
        val signInState = authProvider.signInWithEmailPassword(email, password)

        when (signInState) {
            UiState.Empty,
            UiState.Loading -> Unit

            is UiState.Error -> {
                logger.w { "Sign in error: ${signInState.description}" }
                snackbar.showMessage(signInState.description)
            }

            is UiState.Normal<*> -> {
                analyticsProvider.trackEvent(AnalyticsEvent.SignInSuccess)
                navigator.navigateToHomeGraph()
            }
        }
    }

    fun onSignUpTap() = viewModelScope.launch {
        navigator.navigate(Destination.RegistrationScreen)
    }

    fun onAction(action: AuthScreenAction) {
        when (action) {
            AuthScreenAction.OnSignInWithGoogleTap -> onSignInWithGoogleTap()
            is AuthScreenAction.OnSignInTap -> onSignInTap(action.email, action.password)
            AuthScreenAction.OnSignUpTap -> onSignUpTap()
        }
    }
}
