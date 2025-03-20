package com.eysamarin.squadplay.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.CalendarUI.Date
import com.eysamarin.squadplay.models.EventData
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.EventDialogUI
import com.eysamarin.squadplay.models.Route.Auth
import com.eysamarin.squadplay.models.Route.Profile
import com.eysamarin.squadplay.models.TimeUnit
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth

class MainScreenViewModel(
    private val calendarUIProvider: CalendarUIProvider,
    private val eventProvider: EventProvider,
    private val authProvider: AuthProvider,
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<MainScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    private val _confirmInviteDialogState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val confirmInviteDialogState = _confirmInviteDialogState.asStateFlow()

    private val _inviteGroupIdState = MutableStateFlow<String?>(null)
    val inviteGroupIdState = _inviteGroupIdState.asStateFlow()

    private val _eventDialogState = MutableStateFlow<UiState<EventDialogUI>>(UiState.Empty)
    val eventDialogState = _eventDialogState.asStateFlow()

    private val userInfoState = MutableStateFlow<User?>(null)
    private val eventsState = MutableStateFlow<List<EventData>>(emptyList())
    private val calendarUIState = MutableStateFlow<CalendarUI>(
        calendarUIProvider.provideCalendarUIBy(yearMonth = YearMonth.now())
    )

    init {
        collectUserInfo()
    }

    private fun collectUserInfo() {
        profileProvider.getUserInfoFlow()
            .filterNotNull()
            .onEach {
                Log.d("TAG", "user info received: $it")
                userInfoState.emit(it)
            }
            .combine(inviteGroupIdState.filterNotNull()) { user, inviteGroupId ->
                user to inviteGroupId
            }
            .onEach { (user, inviteGroupId) ->
                if (user.groups.map { it.uid }.contains(inviteGroupId)) {
                    snackbarChannel.send("You're already in this squad")
                    Log.w("TAG", "You're already in this squad")
                    return@onEach
                }

                val groupInfo = profileProvider.getGroupInfo(inviteGroupId)
                if (groupInfo == null) {
                    snackbarChannel.send("Squad with uid: $inviteGroupId not found")
                    Log.w("TAG", "Group with uid: $inviteGroupId not found")
                    return@onEach
                }
                _confirmInviteDialogState.emit(UiState.Normal("Want to join ${groupInfo.title} squad?"))
            }
            .launchIn(viewModelScope)

        combine(
            userInfoState,
            calendarUIState,
            eventsState,
        ) { userInfo, calendar, events ->
            userInfo?.let {
                userInfo to calendar
            }
        }
            .filterNotNull()
            .onEach { (userInfo, calendar) ->
                Log.d("TAG", "updateMainScreenUI")
                _uiState.emit(UiState.Normal(MainScreenUI(user = userInfo, calendarUI = calendar)))
            }
            .launchIn(viewModelScope)
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

    fun onNextMonthTap(nextMonth: YearMonth) = viewModelScope.launch {
        Log.d("TAG", "onNextMonthTap: $nextMonth")

        val nextMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = nextMonth)
        calendarUIState.emit(nextMonthCalendarUI)
    }

    fun onPreviousMonthTap(prevMonth: YearMonth) = viewModelScope.launch {
        Log.d("TAG", "onPreviousMonthTap: $prevMonth")

        val prevMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = prevMonth)
        calendarUIState.emit(prevMonthCalendarUI)
    }

    fun onDateTap(date: Date) = viewModelScope.launch {
        Log.d("TAG", "onDateTap: $date, updating game events")

        val currentCalendar = calendarUIState.value

        val updatedCalendarUI = calendarUIProvider.updateCalendarBySelectedDate(
            target = currentCalendar,
            selectedDate = date,
        )

        calendarUIState.emit(updatedCalendarUI)
    }

    fun dismissEventDialog() = viewModelScope.launch {
        Log.d("TAG", "dismissEventDialog")

        _eventDialogState.emit(UiState.Empty)
    }

    fun onEventSaveTap(
        year: Int,
        month: Int,
        day: Int,
        timeFrom: TimeUnit,
        timeTo: TimeUnit
    ) = viewModelScope.launch {
        Log.d("TAG", "onEventSaveTap")

        val fromDateTime = LocalDateTime.of(year, month, day, timeFrom.hour, timeFrom.minute)
        val toDateTime = LocalDateTime.of(year, month, day, timeTo.hour, timeTo.minute)

        val currentUser = userInfoState.value ?: run {
            Log.w("TAG", "currentUser is null cannot save event")
            return@launch
        }

        if (currentUser.groups.isEmpty()) {
            Log.d("TAG", "currentUser has no groups cannot save event")
            snackbarChannel.send("You have no squads to save event, find your squad first")
            return@launch
        }

        val eventData = EventData(
            creatorId = currentUser.uid,
            groupId = currentUser.groups.first().uid,
            title = "New event",
            fromDateTime = fromDateTime,
            toDateTime = toDateTime,
        )
        val isSuccess = eventProvider.saveEventData(eventData)
        snackbarChannel.send(if (isSuccess) "Event saved successfully" else "Failed to save event")
    }

    fun onAddGameEventTap() = viewModelScope.launch {
        Log.d("TAG", "onAddGameEventTap show polling dialog state")

        val calendarUi = calendarUIState.value
        val selectedDate = calendarUi.dates.firstOrNull { it.enabled && it.isSelected }

        if (selectedDate == null) {
            Log.w("TAG", "selected date is null cannot add game event")
            return@launch
        }

        _eventDialogState.emit(UiState.Normal(EventDialogUI(
            selectedDate = selectedDate,
            yearMonth = calendarUi.yearMonth
        )))
    }

    fun onJoinGroupDeepLinkRetrieved(inviteGroupId: String?) = viewModelScope.launch {
        if (inviteGroupId == null) return@launch

        Log.d("TAG", "onInviteGroupDeepLinkRetrieved: $inviteGroupId")
        _inviteGroupIdState.emit(inviteGroupId)
    }

    fun onJoinGroupDialogConfirm() = viewModelScope.launch {
        Log.d("TAG", "onJoinGroupDialogConfirm")

        val inviteGroupId = inviteGroupIdState.value ?: run {
            Log.w("TAG", "groupId is null, cannot join group")
            return@launch
        }
        val currentUser = userInfoState.value ?: run {
            Log.w("TAG", "currentUser is null, cannot join group")
            return@launch
        }

        val isSuccess = profileProvider.joinGroup(
            userId = currentUser.uid, groupId = inviteGroupId,
        )
        snackbarChannel.send(if (isSuccess) "You joined the squad" else "You failed joining the squad")
    }

    fun onJoinGroupDialogDismiss() = viewModelScope.launch {
        _confirmInviteDialogState.emit(UiState.Empty)
    }
}
