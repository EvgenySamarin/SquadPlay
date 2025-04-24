package com.eysamarin.squadplay.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.CalendarUI.Date
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventDialogUI
import com.eysamarin.squadplay.models.EventUI
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.Route.Auth
import com.eysamarin.squadplay.models.Route.Profile
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

class MainScreenViewModel(
    private val calendarUIProvider: CalendarUIProvider,
    private val eventProvider: EventProvider,
    private val authProvider: AuthProvider,
    private val profileProvider: ProfileProvider,
    private val stringProvider: StringProvider,
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
    private val eventsState = MutableStateFlow<List<Event>>(emptyList())
    private val calendarUIState = MutableStateFlow<CalendarUI>(
        calendarUIProvider.provideCalendarUIBy(yearMonth = YearMonth.now())
    )

    init {
        collectUiStateData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectUiStateData() {
        profileProvider.getUserInfoFlow()
            .onEach {
                if (it == null) {
                    navigationChannel.send(NavAction.NavigateTo(Auth.route))
                }
            }
            .filterNotNull()
            .onEach {
                Log.d("TAG", "user info received: $it")
                userInfoState.emit(it)
            }
            .map { it.groups.firstOrNull() }
            .filterNotNull()
            .flatMapLatest { eventProvider.getEventsFlow(it.uid) }
            .onEach {
                Log.d("TAG", "events received: [${it.firstOrNull()}]...")
                eventsState.emit(it)
            }
            .launchIn(viewModelScope)


        userInfoState
            .filterNotNull()
            .combine(inviteGroupIdState.filterNotNull()) { user, inviteGroupId ->
                user to inviteGroupId
            }
            .onEach { (user, inviteGroupId) ->
                if (user.groups.map { it.uid }.contains(inviteGroupId)) {
                    snackbarChannel.send(stringProvider.alreadyInSquad)
                    Log.w("TAG", "You're already in this squad")
                    return@onEach
                }

                val groupInfo = profileProvider.getGroupInfo(inviteGroupId)
                if (groupInfo == null) {
                    snackbarChannel.send(stringProvider.squadNotFound(inviteGroupId))
                    Log.w("TAG", "Group with uid: $inviteGroupId not found")
                    return@onEach
                }
                _confirmInviteDialogState.emit(UiState.Normal(stringProvider.wantToJoinSquad(groupInfo.title)))
            }
            .launchIn(viewModelScope)

        combine(
            userInfoState,
            calendarUIState,
            eventsState,
        ) { userInfo, calendar, events ->
            userInfo ?: return@combine null

            val eventBasedCalendar = calendarUIProvider.mergedCalendarWithEvents(calendar, events)

            val selectedDate = eventBasedCalendar.dates.firstOrNull { it.isSelected }
            val eventsBySelectedDate = events.filter {
                selectedDate?.dayOfMonth == it.fromDateTime.dayOfMonth
            }.map {
                EventUI(
                    eventId = it.uid,
                    title = it.title,
                    subtitle = stringProvider.fromToDate(
                        fromDate = it.fromDateTime.format(DEFAULT_TIME_FORMATTER),
                        toDate = it.toDateTime.format(DEFAULT_TIME_FORMATTER),
                    ),
                    iconUrl = it.eventIconUrl,
                    isYourEvent = it.creatorId == userInfo.uid
                )
            }
            Triple(userInfo, eventBasedCalendar, eventsBySelectedDate)
        }
            .filterNotNull()
            .onEach { (userInfo, calendar, eventsBySelectedDate) ->
                Log.d("TAG", "updateMainScreenUI")
                _uiState.emit(
                    UiState.Normal(
                        MainScreenUI(
                            user = userInfo,
                            calendarUI = calendar,
                            gameEventsOnDate = eventsBySelectedDate
                        )
                    )
                )
            }
            .flowOn(Dispatchers.IO)
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
        dateTimeFrom: LocalDateTime,
        dateTimeTo: LocalDateTime,
    ) = viewModelScope.launch {
        Log.d("TAG", "onEventSaveTap for dates: $dateTimeFrom - $dateTimeTo")
        val currentUser = userInfoState.value ?: run {
            Log.w("TAG", "currentUser is null cannot save event")
            return@launch
        }

        if (currentUser.groups.isEmpty()) {
            Log.d("TAG", "currentUser has no groups cannot save event")
            snackbarChannel.send(stringProvider.youHaveNoSquad)
            return@launch
        }

        val eventData = Event(
            uid = UUID.randomUUID().toString(),
            creatorId = currentUser.uid,
            groupId = currentUser.groups.first().uid,
            title = "New event",
            fromDateTime = dateTimeFrom,
            toDateTime = dateTimeTo,
        )
        val isSuccess = eventProvider.saveEventData(eventData)
        snackbarChannel.send(
            if (isSuccess) {
                stringProvider.eventSaved
            } else {
                stringProvider.eventSaveFailed
            }
        )
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
        snackbarChannel.send(
            if (isSuccess) {
                stringProvider.joinedSquad
            } else {
                stringProvider.joinSquadFailed
            }
        )
    }

    fun onJoinGroupDialogDismiss() = viewModelScope.launch {
        _confirmInviteDialogState.emit(UiState.Empty)
    }

    fun onDeleteEventTap(eventId: String) = viewModelScope.launch {
        Log.d("TAG", "onDeleteEventTap: $eventId")

        eventProvider.deleteEvent(eventId).also { isSuccess ->
            if (isSuccess) {
                Log.d("TAG", "event deleted successfully")
            } else {
                Log.w("TAG", "failed to delete event")
            }
        }
    }

    companion object {
        val DEFAULT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
