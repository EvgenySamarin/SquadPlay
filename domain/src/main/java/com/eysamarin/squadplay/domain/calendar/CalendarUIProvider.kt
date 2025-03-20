package com.eysamarin.squadplay.domain.calendar

import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.EventData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

interface CalendarUIProvider {
    fun provideCalendarUIBy(yearMonth: YearMonth): CalendarUI
    fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: CalendarUI.Date): CalendarUI
    fun mergedCalendarWithEvents(calendar: CalendarUI, events: List<EventData>): CalendarUI
}

class CalendarUIProviderImpl: CalendarUIProvider {

    private val dataSource by lazy { CalendarDataSource() }
    private val daysOfWeek: List<String> by lazy {
        val daysOfWeek = mutableListOf<String>()

        for (dayOfWeek in DayOfWeek.entries) {
            val localizedDayName = dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.getDefault(),
            )
            daysOfWeek.add(localizedDayName)
        }

        daysOfWeek
    }

    override fun mergedCalendarWithEvents(
        calendar: CalendarUI,
        events: List<EventData>
    ): CalendarUI = calendar.copy(
        dates = calendar.dates.map { date ->
            date.copy(
                countEvents = events.count { event ->
                    val fromDayOfMonth = event.fromDateTime.dayOfMonth
                    val fromMonthOfYear = event.fromDateTime.month.value

                    val isSameDay = fromDayOfMonth == date.dayOfMonth
                            && fromMonthOfYear == calendar.yearMonth.month.value
                    isSameDay
                },
            )
        },
    )

    override fun provideCalendarUIBy(yearMonth: YearMonth): CalendarUI {
        val dates = dataSource.getDates(yearMonth)

        return CalendarUI(
            daysOfWeek = daysOfWeek,
            yearMonth = yearMonth,
            dates = dates
        )
    }

    override fun updateCalendarBySelectedDate(
        target: CalendarUI,
        selectedDate: CalendarUI.Date
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
    fun YearMonth.getDayOfMonthStartingFromMonday(): List<LocalDate> {
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val firstMondayOfMonth = firstDayOfMonth.with(DayOfWeek.MONDAY)
        val firstDayOfNextMonth = firstDayOfMonth.plusMonths(1)

        return generateSequence(firstMondayOfMonth) { it.plusDays(1) }
            .takeWhile { it.isBefore(firstDayOfNextMonth) }
            .toMutableList()
            .apply {
                val extraDaysCount = DayOfWeek.SUNDAY.value - last().dayOfWeek.value
                repeat(extraDaysCount) {
                    add(last().plusDays(1))
                }
            }
            .toList()
    }

    fun getDates(yearMonth: YearMonth): List<CalendarUI.Date> {
        return yearMonth.getDayOfMonthStartingFromMonday()
            .map { date ->
                CalendarUI.Date(
                    dayOfMonth = if (date.monthValue == yearMonth.monthValue) {
                        date.dayOfMonth
                    } else {
                        date.dayOfMonth
                    },
                    isSelected = date.isEqual(LocalDate.now()) && date.monthValue == yearMonth.monthValue,
                    enabled = date.monthValue == yearMonth.monthValue,
                    countEvents = 0,
                )
            }
    }
}
