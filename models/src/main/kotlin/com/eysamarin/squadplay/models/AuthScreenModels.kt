package com.eysamarin.squadplay.models

sealed interface AuthScreenAction {
    object OnSignUpTap : AuthScreenAction
}

data class AuthScreenUI(
    val title: String = "Squad Play",
    val isSignButtonVisible: Boolean = false,
)

val PREVIEW_AUTH_SCREEN_UI = AuthScreenUI(
    isSignButtonVisible = true,
)