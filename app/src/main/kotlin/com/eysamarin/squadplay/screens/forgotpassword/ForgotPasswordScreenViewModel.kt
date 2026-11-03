package com.eysamarin.squadplay.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.ForgotPasswordScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class ForgotPasswordScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val authProvider: AuthProvider,
    private val stringProvider: StringProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    fun onSendResetEmailTap(email: String) = viewModelScope.launch {
        analyticsProvider.trackEvent(AnalyticsEvent.PasswordResetRequested)
        val resetState = authProvider.resetPassword(email)

        when (resetState) {
            UiState.Empty,
            UiState.Loading -> Unit

            is UiState.Error -> {
                logger.w { "Password reset error: ${resetState.description}" }
                snackbar.showMessage(resetState.description)
            }

            is UiState.Normal<*> -> {
                snackbar.showMessage(stringProvider.passwordResetEmailSent)
                navigator.navigateUp()
            }
        }
    }

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }

    fun onAction(action: ForgotPasswordScreenAction) {
        when (action) {
            is ForgotPasswordScreenAction.OnSendResetEmailTap -> onSendResetEmailTap(action.email)
            ForgotPasswordScreenAction.OnBackButtonTap -> onBackButtonTap()
        }
    }
}
