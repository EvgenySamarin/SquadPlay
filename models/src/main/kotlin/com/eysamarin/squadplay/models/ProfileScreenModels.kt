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

val PREVIEW_FRIENDS_GROUP_1 = listOf(
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
)

val PREVIEW_FRIENDS_GROUP_2 = listOf(
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "Harry",
        groupTitleFrom = "Squad Gamers",
        photoUrl = null
    ),
    Friend(
        uid = UUID.randomUUID().toString(),
        username = "Pippin",
        groupTitleFrom = "Squad Gamers",
        photoUrl = null
    ),
)

val PREVIEW_FRIENDS = PREVIEW_FRIENDS_GROUP_1 + PREVIEW_FRIENDS_GROUP_2

val PREVIEW_GROUP_SECTIONS = listOf(
    UserGroupSection(
        groupId = UUID.randomUUID().toString(),
        title = "Friends",
        members = PREVIEW_FRIENDS_GROUP_1
    ),
    UserGroupSection(
        groupId = UUID.randomUUID().toString(),
        title = "Squad Gamers",
        members = PREVIEW_FRIENDS_GROUP_2
    )
)

val PREVIEW_PROFILE_SCREEN_UI = ProfileScreenUI(user = PREVIEW_USER, groupSections = PREVIEW_GROUP_SECTIONS)