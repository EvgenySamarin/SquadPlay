package com.eysamarin.squadplay.models

import java.time.LocalDateTime
import java.time.YearMonth

sealed interface MainScreenAction {
    object OnDismissEventDialog : MainScreenAction
    object OnAddGameEventTap : MainScreenAction
    object OnLogOutTap : MainScreenAction
    object OnAvatarTap : MainScreenAction
    object OnJoinGroupDialogDismiss : MainScreenAction
    object OnJoinGroupDialogConfirm : MainScreenAction

    class OnEventSaveTap(
        val year: Int,
        val month: Int,
        val day: Int,
        val timeFrom: TimeUnit,
        val timeTo: TimeUnit,
    ) : MainScreenAction
    class OnPrevMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnNextMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnDateTap(val date: CalendarUI.Date) : MainScreenAction
}

data class MainScreenUI(
    val user: User,
    val calendarUI: CalendarUI,
    val gameEventsOnDate: List<EventUI> = emptyList(),
)

data class CalendarUI(
    val daysOfWeek: List<String>,
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    data class Date(
        val dayOfMonth: Int? = null,
        val countEvents: Int,
        val isSelected: Boolean,
        val enabled: Boolean,
    ) {
        companion object {
            val Empty =
                Date(dayOfMonth = null, countEvents = 0, isSelected = false, enabled = false)
        }
    }
}

data class EventDialogUI(
    val selectedDate: CalendarUI.Date,
    val yearMonth: YearMonth,
)

data class TimePickerUI(
    val currentTarget: DialPickerTarget = DialPickerTarget.FROM,
    val timeFrom: TimeUnit? = null,
    val timeTo: TimeUnit? = null,
    val errorText: String? = null,
)

data class TimeUnit(
    val hour: Int,
    val minute: Int,
)

enum class DialPickerTarget {
    FROM,
    TO
}

data class Event(
    val creatorId: String,
    val groupId: String,
    val title: String,
    val eventIconUrl: String? = null,
    val fromDateTime: LocalDateTime,
    val toDateTime: LocalDateTime,
)

data class EventUI(
    val title: String,
    val subtitle: String? = null,
    val iconUrl: String? = null,
    val isYourEvent: Boolean = false,
)

val PREVIEW_TIME_PICKER_UI = TimePickerUI(
    currentTarget = DialPickerTarget.FROM,
    timeFrom = TimeUnit(hour = 12, minute = 0),
    timeTo = TimeUnit(hour = 14, minute = 15),
)

val PREVIEW_POLLING_DIALOG_UI = EventDialogUI(
    selectedDate = CalendarUI.Date(
        dayOfMonth = 6,
        countEvents = 4,
        enabled = true,
        isSelected = true,
    ),
    yearMonth = YearMonth.now()
)

val PREVIEW_CALENDAR_UI = CalendarUI(
    daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"),
    yearMonth = YearMonth.now(),
    dates = listOf(
        CalendarUI.Date(1, 0, false, true),
        CalendarUI.Date(2, 0, false, true),
        CalendarUI.Date(3, 1, false, true),
        CalendarUI.Date(4, 0, false, true),
        CalendarUI.Date(5, 0, false, true),
        CalendarUI.Date(6, 5, true, true),
        CalendarUI.Date(7, 0, false, true),
        CalendarUI.Date(8, 0, false, true),
        CalendarUI.Date(9, 0, false, true),
        CalendarUI.Date(10, 0, false, true),
        CalendarUI.Date(11, 0, false, true),
        CalendarUI.Date(12, 3, false, true),
        CalendarUI.Date(13, 0, false, true),
        CalendarUI.Date(14, 0, false, true),
        CalendarUI.Date(15, 0, false, true),
        CalendarUI.Date(16, 0, false, true),
        CalendarUI.Date(17, 2, false, true),
        CalendarUI.Date(18, 0, false, true),
        CalendarUI.Date(19, 0, false, true),
        CalendarUI.Date(20, 0, false, true),
        CalendarUI.Date(21, 0, false, true),
        CalendarUI.Date(22, 0, false, true),
        CalendarUI.Date(23, 0, false, true),
        CalendarUI.Date(24, 0, false, true),
        CalendarUI.Date(25, 0, false, true),
        CalendarUI.Date(26, 5, false, true),
        CalendarUI.Date(27, 0, false, true),
        CalendarUI.Date(28, 0, false, true),
        CalendarUI.Date(29, 1, false, true),
        CalendarUI.Date(30, 0, false, true),
        CalendarUI.Date(31, 0, false, true),
    ),
)

val PREVIEW_EVENTS = listOf(
    Event(
        title = "Dark souls",
        groupId = "groupId",
        eventIconUrl = null,
        creatorId = "creatorId",
        toDateTime = LocalDateTime.now(),
        fromDateTime = LocalDateTime.now().plusHours(1)
    ),
    Event(
        title = "Nino Kuni",
        groupId = "groupId",
        eventIconUrl = null,
        creatorId = "creatorId",
        toDateTime = LocalDateTime.now(),
        fromDateTime = LocalDateTime.now().plusHours(1)
    ),
    Event(
        title = "Dota 2",
        groupId = "groupId",
        eventIconUrl = null,
        creatorId = "creatorId",
        toDateTime = LocalDateTime.now(),
        fromDateTime = LocalDateTime.now().plusHours(1)
    ),
    Event(
        title = "Minecraft",
        groupId = "groupId",
        eventIconUrl = null,
        creatorId = "creatorId",
        toDateTime = LocalDateTime.now(),
        fromDateTime = LocalDateTime.now().plusHours(1)
    ),
    Event(
        title = "Fortnight",
        groupId = "groupId",
        eventIconUrl = null,
        creatorId = "creatorId",
        toDateTime = LocalDateTime.now(),
        fromDateTime = LocalDateTime.now().plusHours(1)
    ),
)

val PREVIEW_MAIN_SCREEN_UI = MainScreenUI(
    user = PREVIEW_USER,
    calendarUI = PREVIEW_CALENDAR_UI
)