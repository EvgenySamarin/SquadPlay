package com.eysamarin.squadplay.models

sealed interface EventDetailsScreenAction {
    data object OnBackButtonTap : EventDetailsScreenAction
}

data class EventDetailsScreenUI(
    val title: String,
    val date: String,
    val imageUrl: String? = null,
)

val PREVIEW_EVENT_DETAILS_SCREEN_UI = EventDetailsScreenUI(
    title = "Apex Legends",
    date = "from 12:00 to 14:00",
    imageUrl = null,
)
