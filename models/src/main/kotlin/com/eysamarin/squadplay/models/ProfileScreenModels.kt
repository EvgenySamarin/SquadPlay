package com.eysamarin.squadplay.models

sealed interface ProfileScreenAction {
    data object OnBackButtonTap: ProfileScreenAction
}

data class ProfileScreenUI(
    val title: String = "Profile",
    val friends: List<String> = listOf("Friend 1", "Friend 2", "Friend 3"),
)

val PREVIEW_PROFILE_SCREEN_UI = ProfileScreenUI()