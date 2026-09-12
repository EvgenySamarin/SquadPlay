package com.eysamarin.squadplay.models

sealed interface EventDetailsScreenAction {
    data object OnBackButtonTap : EventDetailsScreenAction
    data object OnDeleteTap : EventDetailsScreenAction
    data object OnConfirmDeleteTap : EventDetailsScreenAction
    data object OnDismissDeleteDialog : EventDetailsScreenAction
    data object OnAcceptTap : EventDetailsScreenAction
    data object OnRejectTap : EventDetailsScreenAction
}

data class EventMemberUI(
    val uid: String,
    val username: String,
    val photoUrl: String? = null,
    val status: EventResponseStatus = EventResponseStatus.NOT_SET,
)

data class EventDetailsScreenUI(
    val eventId: String,
    val title: String,
    val date: String,
    val imageUrl: String? = null,
    val isYourEvent: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
    val groupId: String = "",
    val members: List<EventMemberUI> = emptyList(),
)

val PREVIEW_EVENT_MEMBERS = listOf(
    EventMemberUI(
        uid = "user-1",
        username = "Alex",
        photoUrl = null,
        status = EventResponseStatus.ACCEPTED,
    ),
    EventMemberUI(
        uid = "user-2",
        username = "Dmitry",
        photoUrl = null,
        status = EventResponseStatus.REJECTED,
    ),
    EventMemberUI(
        uid = "user-3",
        username = "Elena",
        photoUrl = null,
        status = EventResponseStatus.NOT_SET,
    ),
)

val PREVIEW_EVENT_DETAILS_SCREEN_UI = EventDetailsScreenUI(
    eventId = "preview-id",
    title = "Apex Legends",
    date = "from 12:00 to 14:00",
    imageUrl = null,
    isYourEvent = false,
    showDeleteConfirmation = false,
    userStatus = EventResponseStatus.ACCEPTED,
    groupId = "group-1",
    members = PREVIEW_EVENT_MEMBERS,
)

val PREVIEW_CREATOR_EVENT_DETAILS_SCREEN_UI = EventDetailsScreenUI(
    eventId = "preview-id-creator",
    title = "Apex Legends",
    date = "from 12:00 to 14:00",
    imageUrl = null,
    isYourEvent = true,
    showDeleteConfirmation = false,
    userStatus = EventResponseStatus.ACCEPTED,
    groupId = "group-1",
    members = PREVIEW_EVENT_MEMBERS,
)
