package com.eysamarin.squadplay.models

import java.time.LocalDateTime
import java.time.YearMonth

sealed interface NewEventScreenAction {
    data object OnBackButtonTap : NewEventScreenAction
    class OnEventSaveTap(
        val timeFrom: LocalDateTime,
        val timeTo: LocalDateTime,
    ) : NewEventScreenAction
}

data class PickerTimeUnit(
    val hour: Int,
    val minute: Int,
)

data class NewEventScreenUI(
    val title: String,
    val selectedDate: Date,
    val yearMonth: YearMonth,
)

val PREVIEW_TIME_PICKER_UI = TimePickerUI(
    currentTarget = DialPickerTarget.FROM,
    timeFrom = LocalDateTime.of(2025, 4, 1, 12, 0),
    timeTo = LocalDateTime.of(2025, 4, 1, 14, 15),
)

val PREVIEW_NEW_EVENT_SCREEN_UI = NewEventScreenUI(
    title = "New Event",
    selectedDate = Date(
        dayOfMonth = 6,
        countEvents = 4,
        enabled = true,
        isSelected = true,
    ),
    yearMonth = YearMonth.now()
)