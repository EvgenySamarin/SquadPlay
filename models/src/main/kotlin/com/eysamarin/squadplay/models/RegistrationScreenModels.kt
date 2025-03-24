package com.eysamarin.squadplay.models

sealed interface RegistrationScreenAction {
    class OnConfirmTap(val email: String, val password: String) : RegistrationScreenAction
    object OnBackButtonTap : RegistrationScreenAction
}

data class RegistrationScreenUI(
    val title: String = "Registration",
)

val PREVIEW_REGISTRATION_SCREEN_UI = RegistrationScreenUI()