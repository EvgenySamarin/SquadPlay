package com.eysamarin.squadplay.screens.main_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<MainScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val dataSource by lazy { CalendarDataSource() }

    val daysOfWeek: List<String> by lazy {
        val daysOfWeek = mutableListOf<String>()

        for (dayOfWeek in DayOfWeek.entries) {
            val localizedDayName = dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.getDefault(),
            )
            daysOfWeek.add(localizedDayName)
        }

        daysOfWeek
    }

    private fun updateMainScreenUI(updatedMainScreenUI: MainScreenUI) = viewModelScope.launch {
        Log.d("TAG", "updateMainScreenUI by: $updatedMainScreenUI")
        _uiState.emit(UiState.Normal(updatedMainScreenUI))
    }

    fun onNextMonthTap(nextMonth: YearMonth) {
        Log.d("TAG", "onNextMonthTap: $nextMonth")

        val newDates = dataSource.getDates(nextMonth)
        val newMainScreenUI = MainScreenUI(
            calendarUI = CalendarUI(
                daysOfWeek = daysOfWeek,
                yearMonth = nextMonth,
                dates = newDates
            )
        )
        updateMainScreenUI(newMainScreenUI)
    }

    fun onPreviousMonthTap(prevMonth: YearMonth) {
        Log.d("TAG", "onPreviousMonthTap: $prevMonth")

        val newDates = dataSource.getDates(prevMonth)
        val newMainScreenUI = MainScreenUI(
            calendarUI = CalendarUI(
                daysOfWeek = daysOfWeek,
                yearMonth = prevMonth,
                dates = newDates
            )
        )
        updateMainScreenUI(newMainScreenUI)
    }

    fun onDateTap(date: CalendarUI.Date) {
        Log.d("TAG", "onDateTap: $date")
    }

    init {
        viewModelScope.launch {
            val yearMonth = YearMonth.now()
            val dates = dataSource.getDates(yearMonth)
            val calendarState = CalendarUI(
                daysOfWeek = daysOfWeek,
                yearMonth = yearMonth,
                dates = dates
            )

            updateMainScreenUI(MainScreenUI(calendarUI = calendarState))
        }
    }
}

class CalendarDataSource {
    fun YearMonth.getDayOfMonthStartingFromMonday(): List<LocalDate> {
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val firstMondayOfMonth = firstDayOfMonth.with(DayOfWeek.MONDAY)
        val firstDayOfNextMonth = firstDayOfMonth.plusMonths(1)

        return generateSequence(firstMondayOfMonth) { it.plusDays(1) }
            .takeWhile { it.isBefore(firstDayOfNextMonth) }
            .toList()
    }

    fun getDates(yearMonth: YearMonth): List<CalendarUI.Date> {
        return yearMonth.getDayOfMonthStartingFromMonday()
            .map { date ->
                CalendarUI.Date(
                    dayOfMonth = if (date.monthValue == yearMonth.monthValue) {
                        "${date.dayOfMonth}"
                    } else {
                        "" // Fill with empty string for days outside the current month
                    },
                    isSelected = date.isEqual(LocalDate.now()) && date.monthValue == yearMonth.monthValue
                )
            }
    }
}