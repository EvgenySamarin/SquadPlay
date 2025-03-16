package com.eysamarin.squadplay.models

import java.time.YearMonth

sealed interface MainScreenAction {
    object OnDismissPolingDialog : MainScreenAction
    object OnAddGameEventTap : MainScreenAction
    object OnLogOutTap : MainScreenAction
    object OnAvatarTap : MainScreenAction
    class OnPollingStartTap(val timeFrom: TimeUnit, val timeTo: TimeUnit) : MainScreenAction
    class OnPrevMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnNextMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnDateTap(val date: CalendarUI.Date) : MainScreenAction
}

data class MainScreenUI(
    val title: String = "Welcome back, User!",
    val calendarUI: CalendarUI,
    val gameEventsOnDate: List<GameEventUI> = emptyList(),
)

data class CalendarUI(
    val daysOfWeek: List<String>,
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    data class Date(
        val dayOfMonth: String,
        val countEvents: Int,
        val isSelected: Boolean
    ) {
        companion object {
            val Empty = Date(dayOfMonth = "", countEvents = 0, isSelected = false)
        }
    }
}

data class GameEventUI(
    val name: String,
    val players: Int,
    val gameIconResId: Int? = null,
)

data class PollingDialogUI(
    val selectedDate: CalendarUI.Date,
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

val PREVIEW_TIME_PICKER_UI = TimePickerUI(
    currentTarget = DialPickerTarget.FROM,
    timeFrom = TimeUnit(hour = 12, minute = 0),
    timeTo = TimeUnit(hour = 14, minute = 15),
)

val PREVIEW_POLLING_DIALOG_UI = PollingDialogUI(
    selectedDate = CalendarUI.Date("6", 4, true),
)

val PREVIEW_CALENDAR_UI = CalendarUI(
    daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"),
    yearMonth = YearMonth.now(),
    dates = listOf(
        CalendarUI.Date("1", 0, false),
        CalendarUI.Date("2", 0, false),
        CalendarUI.Date("3", 1, false),
        CalendarUI.Date("4", 0, false),
        CalendarUI.Date("5", 0, false),
        CalendarUI.Date("6", 5, true),
        CalendarUI.Date("7", 0, false),
        CalendarUI.Date("8", 0, false),
        CalendarUI.Date("9", 0, false),
        CalendarUI.Date("10", 0, false),
        CalendarUI.Date("11", 0, false),
        CalendarUI.Date("12", 3, false),
        CalendarUI.Date("13", 0, false),
        CalendarUI.Date("14", 0, false),
        CalendarUI.Date("15", 0, false),
        CalendarUI.Date("16", 0, false),
        CalendarUI.Date("17", 2, false),
        CalendarUI.Date("18", 0, false),
        CalendarUI.Date("19", 0, false),
        CalendarUI.Date("20", 0, false),
        CalendarUI.Date("21", 0, false),
        CalendarUI.Date("22", 0, false),
        CalendarUI.Date("23", 0, false),
        CalendarUI.Date("24", 0, false),
        CalendarUI.Date("25", 0, false),
        CalendarUI.Date("26", 5, false),
        CalendarUI.Date("27", 0, false),
        CalendarUI.Date("28", 0, false),
        CalendarUI.Date("29", 1, false),
        CalendarUI.Date("30", 0, false),
        CalendarUI.Date("31", 0, false),
    ),
)

val PREVIEW_GAME_EVENTS = listOf(
    GameEventUI(name = "Dark souls", players = 2),
    GameEventUI(name = "Nino Kuni", players = 3),
    GameEventUI(name = "Dota 2", players = 2),
    GameEventUI(name = "Minecraft", players = 5),
    GameEventUI(name = "Fortnight", players = 7),
)

val PREVIEW_MAIN_SCREEN_UI = MainScreenUI(calendarUI = PREVIEW_CALENDAR_UI)