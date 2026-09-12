package com.eysamarin.squadplay.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

sealed interface HomeScreenAction {
    object OnAddGameEventTap : HomeScreenAction
    object OnLogOutTap : HomeScreenAction
    object OnAvatarTap : HomeScreenAction
    object OnJoinGroupDialogDismiss : HomeScreenAction
    object OnJoinGroupDialogConfirm : HomeScreenAction
    class OnPrevMonthTap(val yearMonth: LocalDate) : HomeScreenAction
    class OnNextMonthTap(val yearMonth: LocalDate) : HomeScreenAction
    class OnDateTap(val date: Date) : HomeScreenAction
    class OnEventTap(val event: EventUI) : HomeScreenAction
}

data class HomeScreenUI(
    val user: User,
    val calendarUI: CalendarUI,
    val gameEventsOnDate: List<EventUI> = emptyList(),
    val isCreateEventButtonVisible: Boolean = true,
)

data class CalendarUI(
    val daysOfWeek: List<String>,
    val yearMonth: LocalDate,
    val dates: List<Date>
)

@Serializable
data class Date(
    val dayOfMonth: Int? = null,
    val monthNumber: Int? = null,
    val countEvents: Int,
    val isSelected: Boolean,
    val enabled: Boolean,
    val hasUserEvents: Boolean = false,
    val year: Int? = null,
) {
    companion object {
        val Empty = Date(
            dayOfMonth = null,
            monthNumber = null,
            countEvents = 0,
            isSelected = false,
            enabled = false,
            hasUserEvents = false,
            year = null,
        )
    }
}

@Serializable
enum class EventResponseStatus {
    ACCEPTED,
    REJECTED,
    NOT_SET,
}

data class Event(
    val uid: String,
    val creatorId: String,
    val groupId: String,
    val title: String,
    val eventIconUrl: String? = null,
    val fromDateTime: LocalDateTime,
    val toDateTime: LocalDateTime,
    val responses: Map<String, String> = emptyMap(),
) {
    fun getStatusForUser(userId: String): EventResponseStatus {
        if (creatorId == userId) return EventResponseStatus.ACCEPTED
        return when (responses[userId]) {
            EventResponseStatus.ACCEPTED.name -> EventResponseStatus.ACCEPTED
            EventResponseStatus.REJECTED.name -> EventResponseStatus.REJECTED
            else -> EventResponseStatus.NOT_SET
        }
    }
}

data class EventUI(
    val eventId: String,
    val title: String,
    val groupTitle: String? = null,
    val groupId: String? = null,
    val subtitle: String? = null,
    val iconUrl: String? = null,
    val isYourEvent: Boolean = false,
    val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
)

val PREVIEW_CALENDAR_UI = CalendarUI(
    daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"),
    yearMonth = java.time.LocalDate.now().run { LocalDate(year, monthValue, 1) },
    dates = listOf(
        Date(1, 12, 0, isSelected = false, enabled = true),
        Date(2, 12, 0, isSelected = false, enabled = true),
        Date(3, 12, 1, isSelected = false, enabled = true, hasUserEvents = true),
        Date(4, 12, 0, isSelected = false, enabled = true),
        Date(5, 12, 0, isSelected = false, enabled = true),
        Date(6, 12, 99, isSelected = true, enabled = true, hasUserEvents = true),
        Date(7, 12, 0, isSelected = false, enabled = true),
        Date(8, 12, 0, isSelected = false, enabled = true),
        Date(9, 12, 0, isSelected = false, enabled = true),
        Date(10, 12, 0, isSelected = false, enabled = true),
        Date(11, 12, 0, isSelected = false, enabled = true),
        Date(12, 12, 3, isSelected = false, enabled = true, hasUserEvents = true),
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
        Date(26, 12, 5, isSelected = false, enabled = true, hasUserEvents = true),
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
        userStatus = EventResponseStatus.ACCEPTED,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Nino Kuni",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = false,
        userStatus = EventResponseStatus.REJECTED,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Dota 2",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = true,
        userStatus = EventResponseStatus.NOT_SET,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Minecraft",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = false,
        userStatus = EventResponseStatus.NOT_SET,
    ),
    EventUI(
        eventId = UUID.randomUUID().toString(),
        title = "Fortnight",
        iconUrl = null,
        subtitle = "from 12:00 to 14:00",
        isYourEvent = true,
        userStatus = EventResponseStatus.NOT_SET,
    ),
)

val PREVIEW_MAIN_SCREEN_UI = HomeScreenUI(
    user = PREVIEW_USER,
    calendarUI = PREVIEW_CALENDAR_UI,
    gameEventsOnDate = PREVIEW_EVENTS,
)