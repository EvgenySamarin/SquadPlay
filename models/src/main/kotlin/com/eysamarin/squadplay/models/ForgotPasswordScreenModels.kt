package com.eysamarin.squadplay.models

sealed interface ForgotPasswordScreenAction {
    class OnSendResetEmailTap(val email: String) : ForgotPasswordScreenAction
    object OnBackButtonTap : ForgotPasswordScreenAction
}
