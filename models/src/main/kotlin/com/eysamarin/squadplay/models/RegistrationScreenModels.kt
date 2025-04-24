package com.eysamarin.squadplay.models

sealed interface RegistrationScreenAction {
    class OnConfirmTap(val email: String, val password: String) : RegistrationScreenAction
    object OnBackButtonTap : RegistrationScreenAction
}