package com.eysamarin.squadplay.models

sealed interface EventDetailsScreenAction {
    data object OnBackButtonTap : EventDetailsScreenAction
    data object OnDeleteTap : EventDetailsScreenAction
    data object OnConfirmDeleteTap : EventDetailsScreenAction
    data object OnDismissDeleteDialog : EventDetailsScreenAction
    data object OnAcceptTap : EventDetailsScreenAction
    data object OnRejectTap : EventDetailsScreenAction
}

data class EventDetailsScreenUI(
    val eventId: String,
    val title: String,
    val date: String,
    val imageUrl: String? = null,
    val isYourEvent: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
)

val PREVIEW_EVENT_DETAILS_SCREEN_UI = EventDetailsScreenUI(
    eventId = "preview-id",
    title = "Apex Legends",
    date = "from 12:00 to 14:00",
    imageUrl = null,
    isYourEvent = false,
    showDeleteConfirmation = false,
    userStatus = EventResponseStatus.ACCEPTED,
)

val PREVIEW_CREATOR_EVENT_DETAILS_SCREEN_UI = EventDetailsScreenUI(
    eventId = "preview-id-creator",
    title = "Apex Legends",
    date = "from 12:00 to 14:00",
    imageUrl = null,
    isYourEvent = true,
    showDeleteConfirmation = false,
    userStatus = EventResponseStatus.ACCEPTED,
)
