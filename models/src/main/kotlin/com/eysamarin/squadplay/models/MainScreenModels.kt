package com.eysamarin.squadplay.models

import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.util.UUID

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
    class OnDeleteEventTap(val eventId: String): MainScreenAction
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
        val month: Month? = null,
        val countEvents: Int,
        val isSelected: Boolean,
        val enabled: Boolean,
    ) {
        companion object {
            val Empty = Date(
                dayOfMonth = null,
                month = null,
                countEvents = 0,
                isSelected = false,
                enabled = false,
            )
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
    val uid: String,
    val creatorId: String,
    val groupId: String,
    val title: String,
    val eventIconUrl: String? = null,
    val fromDateTime: LocalDateTime,
    val toDateTime: LocalDateTime,
)

data class EventUI(
    val eventId: String,
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
        CalendarUI.Date(1, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(2, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(3, Month.DECEMBER, 1, false, true),
        CalendarUI.Date(4, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(5, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(6, Month.DECEMBER, 5, true, true),
        CalendarUI.Date(7, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(8, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(9, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(10, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(11, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(12, Month.DECEMBER, 3, false, true),
        CalendarUI.Date(13, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(14, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(15, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(16, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(17, Month.DECEMBER, 2, false, true),
        CalendarUI.Date(18, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(19, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(20, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(21, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(22, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(23, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(24, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(25, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(26, Month.DECEMBER, 5, false, true),
        CalendarUI.Date(27, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(28, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(29, Month.DECEMBER, 1, false, true),
        CalendarUI.Date(30, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(31, Month.DECEMBER, 0, false, true),
        CalendarUI.Date(1, Month.JANUARY, 0, false, false),
        CalendarUI.Date(2, Month.JANUARY, 0, false, false),
        CalendarUI.Date(3, Month.JANUARY, 1, false, false),
        CalendarUI.Date(4, Month.JANUARY, 0, false, false),
    ),
)

val PREVIEW_EVENTS = listOf(
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Dark souls",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = false,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Nino Kuni",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = false,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Dota 2",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = true,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Minecraft",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = false,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Fortnight",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = true,
    ),
)

val PREVIEW_MAIN_SCREEN_UI = MainScreenUI(
    user = PREVIEW_USER,
    calendarUI = PREVIEW_CALENDAR_UI
)