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
)

val PREVIEW_NEW_EVENT_SCREEN_UI = NewEventScreenUI(
    title = "New Event",
    selectedDate = Date(
        dayOfMonth = 6,
        countEvents = 4,
        enabled = true,
        isSelected = true,
    ),
    yearMonth = java.time.LocalDate.now().run { LocalDate(year, monthValue, 1) }
)