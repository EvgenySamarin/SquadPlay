package com.eysamarin.squadplay.domain.calendar

import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.Friend
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import java.time.format.TextStyle
import java.util.Locale

interface CalendarUIProvider {
    fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI
    fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI
    fun mergedCalendarWithEvents(
        calendar: CalendarUI,
        events: List<Event>,
        currentUserId: String,
        groupMembers: Map<String, List<Friend>>
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

    /**
     * Merges event statistics into the provided [CalendarUI].
     *
     * Performance optimizations:
     * - Uses a primitive [Int] key via [dateKey] to prevent object allocations.
     * - Uses [MutableDateSummary] accumulator to avoid per-event allocations.
     * - Evaluates flags lazily to skip redundant computations.
     */
    override fun mergedCalendarWithEvents(
        calendar: CalendarUI,
        events: List<Event>,
        currentUserId: String,
        groupMembers: Map<String, List<Friend>>,
    ): CalendarUI {
        if (events.isEmpty()) {
            val hasAnyEvents = calendar.dates.any { it.countEvents > 0 || it.hasUserEvents || it.hasAdminEvents }
            if (!hasAnyEvents) return calendar
            return calendar.copy(
                dates = calendar.dates.map { date ->
                    if (date.countEvents == 0 && !date.hasUserEvents && !date.hasAdminEvents) date
                    else date.copy(countEvents = 0, hasUserEvents = false, hasAdminEvents = false)
                }
            )
        }

        val eventsByDate = mutableMapOf<Int, MutableDateSummary>()

        for (event in events) {
            val key = dateKey(event.fromDateTime.year, event.fromDateTime.month.number, event.fromDateTime.day)
            val summary = eventsByDate.getOrPut(key) { MutableDateSummary() }

            summary.count++

            // Lazy evaluation: skip setting if user event status is already true for this date
            if (!summary.hasUserEvents && event.creatorId == currentUserId) {
                summary.hasUserEvents = true
            }

            // Lazy evaluation: skip expensive member acceptance checks if an event on this date is already accepted by all
            if (!summary.hasAdminEvents) {
                val members = groupMembers[event.groupId]
                if (!members.isNullOrEmpty() && members.all { event.getStatusForUser(it.uid) == EventResponseStatus.ACCEPTED }) {
                    summary.hasAdminEvents = true
                }
            }
        }

        return calendar.copy(
            dates = calendar.dates.map { date ->
                val day = date.dayOfMonth
                val month = date.monthNumber
                val year = date.year ?: calendar.yearMonth.year
                if (day != null && month != null) {
                    val key = dateKey(year, month, day)
                    val summary = eventsByDate[key]
                    date.copy(
                        countEvents = summary?.count ?: 0,
                        hasUserEvents = summary?.hasUserEvents ?: false,
                        hasAdminEvents = summary?.hasAdminEvents ?: false
                    )
                } else {
                    date.copy(countEvents = 0, hasUserEvents = false, hasAdminEvents = false)
                }
            }
        )
    }

    /**
     * Mutable accumulator used during calendar event merging to avoid heap allocation
     * of summary data objects on each event iteration.
     */
    private class MutableDateSummary {
        var count: Int = 0
        var hasUserEvents: Boolean = false
        var hasAdminEvents: Boolean = false
    }

    /**
     * Encodes year, month, and day into a primitive [Int] key (format: YYYYMMDD).
     *
     * Using a primitive integer key avoids object allocation of [Triple] or [LocalDate]
     * instances during event grouping.
     */
    private fun dateKey(year: Int, month: Int, day: Int): Int = year * 10000 + month * 100 + day

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
                    item.isSelected -> item.copy(isSelected = false)
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
                    dayOfMonth = date.day,
                    monthNumber = date.month.number,
                    isSelected = date == today && date.month.number == yearMonth.month.number,
                    enabled = date.month.number == yearMonth.month.number,
                    countEvents = 0,
                    year = date.year,
                )
            }
    }
}
