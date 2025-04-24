package com.eysamarin.squadplay.models

sealed interface AuthScreenAction {
    object OnSignInWithGoogleTap : AuthScreenAction
    class OnSignInTap(val email: String, val password: String) : AuthScreenAction
    object OnSignUpTap : AuthScreenAction
}