package com.eysamarin.squadplay.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

sealed interface HomeScreenAction {
    object OnAddGameEventTap : HomeScreenAction
    object OnLogOutTap : HomeScreenAction
    object OnAvatarTap : HomeScreenAction
    object OnJoinGroupDialogDismiss : HomeScreenAction
    object OnJoinGroupDialogConfirm : HomeScreenAction
    class OnPrevMonthTap(val yearMonth: YearMonth) : HomeScreenAction
    class OnNextMonthTap(val yearMonth: YearMonth) : HomeScreenAction
    class OnDateTap(val date: Date) : HomeScreenAction
    class OnDeleteEventTap(val eventId: String): HomeScreenAction
}

data class HomeScreenUI(
    val user: User,
    val calendarUI: CalendarUI,
    val gameEventsOnDate: List<EventUI> = emptyList(),
)

data class CalendarUI(
    val daysOfWeek: List<String>,
    val yearMonth: YearMonth,
    val dates: List<Date>
)

@Serializable
data class Date(
    val dayOfMonth: Int? = null,
    val monthNumber: Int? = null,
    val countEvents: Int,
    val isSelected: Boolean,
    val enabled: Boolean,
) {
    companion object {
        val Empty = Date(
            dayOfMonth = null,
            monthNumber = null,
            countEvents = 0,
            isSelected = false,
            enabled = false,
        )
    }
}

data class TimePickerUI(
    val currentTarget: DialPickerTarget = DialPickerTarget.FROM,
    val timeFrom: LocalDateTime? = null,
    val timeTo: LocalDateTime? = null,
    val errorText: String? = null,
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

val PREVIEW_CALENDAR_UI = CalendarUI(
    daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"),
    yearMonth = YearMonth.now(),
    dates = listOf(
        Date(1, 12, 0, isSelected = false, enabled = true),
        Date(2, 12, 0, isSelected = false, enabled = true),
        Date(3, 12, 1, isSelected = false, enabled = true),
        Date(4, 12, 0, isSelected = false, enabled = true),
        Date(5, 12, 0, isSelected = false, enabled = true),
        Date(6, 12, 5, isSelected = true, enabled = true),
        Date(7, 12, 0, isSelected = false, enabled = true),
        Date(8, 12, 0, isSelected = false, enabled = true),
        Date(9, 12, 0, isSelected = false, enabled = true),
        Date(10, 12, 0, isSelected = false, enabled = true),
        Date(11, 12, 0, isSelected = false, enabled = true),
        Date(12, 12, 3, isSelected = false, enabled = true),
        Date(13, 12, 0, isSelected = false, enabled = true),
        Date(14, 12, 0, isSelected = false, enabled = true),
        Date(15, 12, 0, isSelected = false, enabled = true),
        Date(16, 12, 0, isSelected = false, enabled = true),
        Date(17, 12, 2, isSelected = false, enabled = true),
        Date(18, 12, 0, isSelected = false, enabled = true),
        Date(19, 12, 0, isSelected = false, enabled = true),
        Date(20, 12, 0, isSelected = false, enabled = true),
        Date(21, 12, 0, isSelected = false, enabled = true),
        Date(22, 12, 0, isSelected = false, enabled = true),
        Date(23, 12, 0, isSelected = false, enabled = true),
        Date(24, 12, 0, isSelected = false, enabled = true),
        Date(25, 12, 0, isSelected = false, enabled = true),
        Date(26, 12, 5, isSelected = false, enabled = true),
        Date(27, 12, 0, isSelected = false, enabled = true),
        Date(28, 12, 0, isSelected = false, enabled = true),
        Date(29, 12, 1, isSelected = false, enabled = true),
        Date(30, 12, 0, isSelected = false, enabled = true),
        Date(31, 12, 0, isSelected = false, enabled = true),
        Date(1, 1, 0, isSelected = false, enabled = false),
        Date(2, 1, 0, isSelected = false, enabled = false),
        Date(3, 1, 1, isSelected = false, enabled = false),
        Date(4, 1, 0, isSelected = false, enabled = false),
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

val PREVIEW_MAIN_SCREEN_UI = HomeScreenUI(
    user = PREVIEW_USER,
    calendarUI = PREVIEW_CALENDAR_UI
)