package com.eysamarin.squadplay.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate

sealed interface NewEventScreenAction {
    data object OnBackButtonTap : NewEventScreenAction
    class OnEventSaveTap(
        val title: String,
        val timeFrom: LocalDateTime,
        val timeTo: LocalDateTime,
        val eventIconUrl: String?,
        val groupId: String,
    ) : NewEventScreenAction
    class OnGameTitleChanged(val title: String) : NewEventScreenAction
}

data class PickerTimeUnit(
    val hour: Int,
    val minute: Int,
)

data class NewEventScreenUI(
    val title: String,
    val selectedDate: Date,
    val yearMonth: LocalDate,
    val gameTitle: String = "",
    val eventIconUrl: String? = null,
    val userGroups: List<Group> = emptyList(),
)

val PREVIEW_NEW_EVENT_SCREEN_UI = NewEventScreenUI(
    title = "New Event",
    selectedDate = Date(
        dayOfMonth = 6,
        countEvents = 4,
        enabled = true,
        isSelected = true,
    ),
    yearMonth = java.time.LocalDate.now().run { LocalDate(year, monthValue, 1) },
    userGroups = listOf(
        Group(uid = "group-1", title = "Warriors", members = listOf("user-1")),
        Group(uid = "group-2", title = "Mages", members = listOf("user-1", "user-2")),
    ),
)