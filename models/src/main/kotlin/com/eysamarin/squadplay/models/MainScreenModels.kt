package com.eysamarin.squadplay.models

import java.time.YearMonth

sealed interface MainScreenAction {
    object OnDismissPolingDialog: MainScreenAction
    object OnPollingStartTap: MainScreenAction
    class OnPrevMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnNextMonthTap(val yearMonth: YearMonth) : MainScreenAction
    class OnDateTap(val date: CalendarUI.Date) : MainScreenAction
}

data class MainScreenUI(
    val title: String = "Давай прокачаемся вместе, User!",
    val calendarUI: CalendarUI,
)

data class CalendarUI(
    val daysOfWeek: List<String>,
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    data class Date(
        val dayOfMonth: String,
        val isSelected: Boolean
    ) {
        companion object {
            val Empty = Date("", false)
        }
    }
}

data class PollingDialogUI(
    val selectedDate: CalendarUI.Date,
)

val PREVIEW_CALENDAR_UI = CalendarUI(
    yearMonth = YearMonth.now(),
    daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"),
    dates = listOf(
        CalendarUI.Date("1", false),
        CalendarUI.Date("2", false),
        CalendarUI.Date("3", false),
        CalendarUI.Date("4", false),
        CalendarUI.Date("5", false),
        CalendarUI.Date("6", true),
        CalendarUI.Date("7", false),
        CalendarUI.Date("8", false),
        CalendarUI.Date("9", false),
        CalendarUI.Date("10", false),
        CalendarUI.Date("11", false),
        CalendarUI.Date("12", false),
        CalendarUI.Date("13", false),
        CalendarUI.Date("14", false),
        CalendarUI.Date("15", false),
        CalendarUI.Date("16", false),
        CalendarUI.Date("17", false),
        CalendarUI.Date("18", false),
        CalendarUI.Date("19", false),
        CalendarUI.Date("20", false),
        CalendarUI.Date("21", false),
        CalendarUI.Date("22", false),
        CalendarUI.Date("23", false),
        CalendarUI.Date("24", false),
        CalendarUI.Date("25", false),
        CalendarUI.Date("26", false),
        CalendarUI.Date("27", false),
        CalendarUI.Date("28", false),
        CalendarUI.Date("29", false),
        CalendarUI.Date("30", false),
        CalendarUI.Date("31", false),
    ),
)

val PREVIEW_MAIN_SCREEN_UI = MainScreenUI(calendarUI = PREVIEW_CALENDAR_UI)