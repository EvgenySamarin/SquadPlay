package com.eysamarin.squadplay.domain.calendar

import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.Event
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.time.format.TextStyle
import java.util.Locale

interface CalendarUIProvider {
    fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI
    fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI
    fun mergedCalendarWithEvents(
        calendar: CalendarUI,
        events: List<Event>,
        currentUserId: String
    ): CalendarUI
}

class CalendarUIProviderImpl: CalendarUIProvider {

    private val dataSource by lazy { CalendarDataSource() }
    private val daysOfWeek: List<String> by lazy {
        val daysOfWeek = mutableListOf<String>()

        for (dayOfWeek in DayOfWeek.entries) {
            // Convert kotlinx.datetime.DayOfWeek to java.time.DayOfWeek for display name
            val javaDayOfWeek = java.time.DayOfWeek.valueOf(dayOfWeek.name)
            val localizedDayName = javaDayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.getDefault(),
            )
            daysOfWeek.add(localizedDayName)
        }

        daysOfWeek
    }

    override fun mergedCalendarWithEvents(
        calendar: CalendarUI,
        events: List<Event>,
        currentUserId: String
    ): CalendarUI = calendar.copy(
        dates = calendar.dates.map { date ->
            val eventsOnDate = events.filter { event ->
                val fromDayOfMonth = event.fromDateTime.dayOfMonth
                val fromMonthOfYear = event.fromDateTime.month.ordinal + 1

                val isSameDay = fromDayOfMonth == date.dayOfMonth
                        && fromMonthOfYear == date.monthNumber
                isSameDay
            }
            date.copy(
                countEvents = eventsOnDate.count(),
                hasUserEvents = eventsOnDate.any { it.creatorId == currentUserId }
            )
        },
    )

    override fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI {
        val dates = dataSource.getDates(yearMonth)

        return CalendarUI(
            daysOfWeek = daysOfWeek,
            yearMonth = yearMonth,
            dates = dates
        )
    }

    override fun updateCalendarBySelectedDate(
        target: CalendarUI,
        selectedDate: Date
    ): CalendarUI {
        return target.copy(
            dates = target.dates.map { item ->
                when {
                    item.dayOfMonth == selectedDate.dayOfMonth && item.enabled-> item.copy(isSelected = true)
                    item.isSelected == true -> item.copy(isSelected = false)
                    else -> item
                }
            }
        )
    }
}


class CalendarDataSource {
    fun LocalDate.getDayOfMonthStartingFromMonday(): List<LocalDate> {
        val firstDayOfMonth = LocalDate(year, month, 1)
        val daysToSubtract = (firstDayOfMonth.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
        val firstMondayOfMonth = firstDayOfMonth.minus(daysToSubtract, DateTimeUnit.DAY)
        val firstDayOfNextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)

        return generateSequence(firstMondayOfMonth) { it.plus(1, DateTimeUnit.DAY) }
            .takeWhile { it < firstDayOfNextMonth }
            .toMutableList()
            .apply {
                while (last().dayOfWeek != DayOfWeek.SUNDAY) {
                    add(last().plus(1, DateTimeUnit.DAY))
                }
            }
            .toList()
    }

    fun getDates(yearMonth: LocalDate): List<Date> {
        val today = java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
        return yearMonth.getDayOfMonthStartingFromMonday()
            .map { date ->
                Date(
                    dayOfMonth = date.dayOfMonth,
                    monthNumber = date.monthNumber,
                    isSelected = date == today && date.monthNumber == yearMonth.monthNumber,
                    enabled = date.monthNumber == yearMonth.monthNumber,
                    countEvents = 0,
                )
            }
    }
}
