package com.eysamarin.squadplay.models

import java.util.UUID

sealed interface ProfileScreenAction {
    data object OnBackButtonTap : ProfileScreenAction
    object OnAddNewFriendTap : ProfileScreenAction
}

data class ProfileScreenUI(
    val user: User,
)

val PREVIEW_USER = User(
    uid = UUID.randomUUID().toString(),
    username = "Peter Parker",
    email = "peter.parker@gmail.com",
    photoUrl = null,
    friends = listOf(
        Friend(uid = UUID.randomUUID().toString(), username = "NexArt"),
        Friend(uid = UUID.randomUUID().toString(), username = "Alibaba"),
        Friend(uid = UUID.randomUUID().toString(), username = "Harry"),
        Friend(uid = UUID.randomUUID().toString(), username = "Pippin"),
    )
)

val PREVIEW_PROFILE_SCREEN_UI = ProfileScreenUI(user = PREVIEW_USER)