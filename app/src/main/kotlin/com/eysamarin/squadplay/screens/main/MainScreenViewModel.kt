package com.eysamarin.squadplay.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.event.GameEventUIProvider
import com.eysamarin.squadplay.domain.polling.PollingProvider
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.PollingDialogUI
import com.eysamarin.squadplay.models.Routes.Auth
import com.eysamarin.squadplay.models.Routes.Profile
import com.eysamarin.squadplay.models.TimeUnit
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class MainScreenViewModel(
    private val calendarUIProvider: CalendarUIProvider,
    private val gameEventUIProvider: GameEventUIProvider,
    private val pollingProvider: PollingProvider,
    private val authProvider: AuthProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<MainScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _pollingDialogState = MutableStateFlow<UiState<PollingDialogUI>>(UiState.Empty)
    val pollingDialogState = _pollingDialogState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val calendarState = calendarUIProvider.provideCalendarUIBy(yearMonth = YearMonth.now())
            updateMainScreenUI(MainScreenUI(calendarUI = calendarState))
        }
    }

    private fun updateMainScreenUI(updatedMainScreenUI: MainScreenUI) = viewModelScope.launch {
        Log.d("TAG", "updateMainScreenUI by: $updatedMainScreenUI")
        _uiState.emit(UiState.Normal(updatedMainScreenUI))
    }

    fun onLogOutTap() = viewModelScope.launch {
        Log.d("TAG", "onLogOutTap")
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            navigationChannel.send(NavAction.NavigateTo(Auth.route))
        } else {
            Log.d("TAG", "cannot log out")
        }
    }

    fun onAvatarTap() = viewModelScope.launch {
        Log.d("TAG", "onAvatarTap")
        navigationChannel.send(NavAction.NavigateTo(Profile.route))
    }

    fun onNextMonthTap(nextMonth: YearMonth) {
        Log.d("TAG", "onNextMonthTap: $nextMonth")

        val nextMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = nextMonth)
        val newMainScreenUI = MainScreenUI(calendarUI = nextMonthCalendarUI)
        updateMainScreenUI(newMainScreenUI)
    }

    fun onPreviousMonthTap(prevMonth: YearMonth) {
        Log.d("TAG", "onPreviousMonthTap: $prevMonth")

        val prevMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = prevMonth)
        val newMainScreenUI = MainScreenUI(calendarUI = prevMonthCalendarUI)
        updateMainScreenUI(newMainScreenUI)
    }

    fun onDateTap(date: CalendarUI.Date) = viewModelScope.launch {
        Log.d("TAG", "onDateTap: $date, updating game events")

        val currentMainScreenUI = (uiState.value as? UiState.Normal<MainScreenUI>)?.data
        if (currentMainScreenUI == null) {
            Log.w("TAG", "current UI cannot be updated since of no data")
            return@launch
        }

        val updatedCalendarUI = calendarUIProvider.updateCalendarBySelectedDate(
            target = currentMainScreenUI.calendarUI,
            selectedDate = date,
        )

        val gameEvents = gameEventUIProvider.provideGameEventUIBy(date)
        updateMainScreenUI(
            updatedMainScreenUI = currentMainScreenUI.copy(
                calendarUI = updatedCalendarUI,
                gameEventsOnDate = gameEvents,
            )
        )
    }

    fun dismissPolingDialog() = viewModelScope.launch {
        Log.d("TAG", "dismissPolingDialog")

        _pollingDialogState.emit(UiState.Empty)
    }

    fun onPollingStartTap(timeFrom: TimeUnit, timeTo: TimeUnit) {
        val selectedDate = takeSelectedDate()

        Log.d("TAG", "onPollingStartTap for date: $selectedDate, timeFrom: $timeFrom, timeTo: $timeTo")
        pollingProvider.savePollingData("timeFrom: $timeFrom, timeTo: $timeTo")
    }

    fun onAddGameEventTap() = viewModelScope.launch {
        Log.d("TAG", "onAddGameEventTap show polling dialog state")

        val currentSelectedDate = takeSelectedDate()

        if (currentSelectedDate == null) {
            Log.w("TAG", "selected date is null cannot add game event")
            return@launch
        }

        _pollingDialogState.emit(UiState.Normal(PollingDialogUI(selectedDate = currentSelectedDate)))
    }

    private fun takeSelectedDate(): CalendarUI.Date? =
        (uiState.value as? UiState.Normal<MainScreenUI>)
            ?.data
            ?.calendarUI
            ?.dates
            ?.firstOrNull { it.isSelected }
}
