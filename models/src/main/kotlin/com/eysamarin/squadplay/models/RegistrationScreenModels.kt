package com.eysamarin.squadplay.models

sealed interface RegistrationScreenAction {
    object OnConfirmTap : RegistrationScreenAction
    object OnBackButtonTap : RegistrationScreenAction
}

data class RegistrationScreenUI(
    val title: String = "Registration",
)

val PREVIEW_REGISTRATION_SCREEN_UI = RegistrationScreenUI()