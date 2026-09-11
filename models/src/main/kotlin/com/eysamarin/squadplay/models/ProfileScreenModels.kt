package com.eysamarin.squadplay.models

import java.util.UUID

sealed interface ProfileScreenAction {
    data object OnBackButtonTap : ProfileScreenAction
    data class OnCreateInviteLinkTap(val groupId: String) : ProfileScreenAction
    object OnLogOutTap : ProfileScreenAction
    object OnSettingsTap : ProfileScreenAction
}

data class ProfileScreenUI(
    val user: User,
    val groupSections: List<UserGroupSection>,
)

val PREVIEW_USER = User(
    uid = UUID.randomUUID().toString(),
    username = "Peter Parker",
    email = "peter.parker@gmail.com",
    photoUrl = null,
    groups = emptyList(),
)

val PREVIEW_FRIENDS = listOf(
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "NexArt",
        groupTitleFrom = "Friends",
        photoUrl = null
    ),
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "Alibaba",
        groupTitleFrom = "Friends",
        photoUrl = null
    ),
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "Harry",
        groupTitleFrom = "Friends",
        photoUrl = null
    ),
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "Pippin",
        groupTitleFrom = "Friends",
        photoUrl = null
    ),
)

val PREVIEW_GROUP_SECTIONS = listOf(
    UserGroupSection(
        groupId = UUID.randomUUID().toString(),
        title = "Friends",
        members = PREVIEW_FRIENDS
    )
)

val PREVIEW_PROFILE_SCREEN_UI = ProfileScreenUI(user = PREVIEW_USER, groupSections = PREVIEW_GROUP_SECTIONS)