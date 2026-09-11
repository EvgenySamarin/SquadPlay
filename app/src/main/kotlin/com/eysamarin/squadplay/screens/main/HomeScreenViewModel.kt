package com.eysamarin.squadplay.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventUI
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.HomeScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.navigation.DeepLinkManager
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

class HomeScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val calendarUIProvider: CalendarUIProvider,
    private val eventProvider: EventProvider,
    private val authProvider: AuthProvider,
    private val profileProvider: ProfileProvider,
    private val stringProvider: StringProvider,
    private val deepLinkManager: DeepLinkManager,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    companion object {
        internal var defaultIoDispatcher: CoroutineDispatcher = Dispatchers.IO
        internal var defaultTodayProvider: () -> LocalDate = {
            java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
        }
    }

    internal var ioDispatcher: CoroutineDispatcher = defaultIoDispatcher
    internal var todayProvider: () -> LocalDate = defaultTodayProvider

    val uiState: StateFlow<UiState<HomeScreenUI>>
        field = MutableStateFlow<UiState<HomeScreenUI>>(UiState.Loading)

    val isLoggingOut: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)

    val confirmInviteDialogState: StateFlow<UiState<String>>
        field = MutableStateFlow<UiState<String>>(UiState.Empty)

    val inviteGroupIdState: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    private val userInfoState = MutableStateFlow<User?>(null)
    private val eventsState = MutableStateFlow<List<Event>>(emptyList())
    private val calendarUIState = MutableStateFlow<CalendarUI>(
        calendarUIProvider.provideCalendarUIBy(yearMonth = todayProvider().run { LocalDate(year, month.number, 1) })
    )

    init {
        collectUiStateData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectUiStateData() {
        viewModelScope.launch {
            deepLinkManager.pendingInviteGroupId
                .filterNotNull()
                .collect { inviteGroupId ->
                    deepLinkManager.consumePendingInviteGroupId()
                    onJoinGroupDeepLinkRetrieved(inviteGroupId)
                }
        }

        profileProvider.getUserInfoFlow()
            .onEach {
                logger.d { "New user fetched: $it" }
                if (it == null) {
                    navigator.navigateToAuthGraph()
                }
            }
            .filterNotNull()
            .onEach {
                logger.d { "User info received: $it" }
                userInfoState.emit(it)
            }
            .mapNotNull { it.groups.firstOrNull() }
            .flatMapLatest { eventProvider.getEventsFlow(it.uid) }
            .onEach {
                logger.d { "Events received: [${it.firstOrNull()}]..." }
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
                    snackbar.showMessage(stringProvider.alreadyInSquad)
                    logger.w { "You're already in this squad" }
                    return@onEach
                }

                val groupInfo = profileProvider.getGroupInfo(inviteGroupId)
                if (groupInfo == null) {
                    snackbar.showMessage(stringProvider.squadNotFound(inviteGroupId))
                    logger.w { "Group with uid: $inviteGroupId not found" }
                    return@onEach
                }
                confirmInviteDialogState.emit(UiState.Normal(stringProvider.wantToJoinSquad(groupInfo.title)))
            }
            .launchIn(viewModelScope)

        combine(
            userInfoState,
            calendarUIState,
            eventsState,
        ) { userInfo, calendar, events ->
            userInfo ?: return@combine null

            val eventBasedCalendar = calendarUIProvider.mergedCalendarWithEvents(
                calendar = calendar,
                events = events,
                currentUserId = userInfo.uid
            )

            val selectedDate = eventBasedCalendar.dates.firstOrNull { it.isSelected }
            val eventsBySelectedDate = events.filter {
                selectedDate?.dayOfMonth == it.fromDateTime.day
                        && selectedDate.monthNumber == it.fromDateTime.month.number
            }.map {
                EventUI(
                    eventId = it.uid,
                    title = it.title,
                    subtitle = stringProvider.fromToDate(
                        fromDate = formatTime(it.fromDateTime.hour, it.fromDateTime.minute),
                        toDate = formatTime(it.toDateTime.hour, it.toDateTime.minute),
                    ),
                    iconUrl = it.eventIconUrl,
                    isYourEvent = it.creatorId == userInfo.uid
                )
            }
            val today = todayProvider()
            val dayOfMonth = selectedDate?.dayOfMonth
            val isCreateEventButtonVisible = if (selectedDate != null && dayOfMonth != null && selectedDate.enabled) {
                val selectedLocalDate = LocalDate(
                    year = eventBasedCalendar.yearMonth.year,
                    monthNumber = selectedDate.monthNumber ?: eventBasedCalendar.yearMonth.month.number,
                    dayOfMonth = dayOfMonth,
                )
                selectedLocalDate >= today
            } else {
                false
            }
            HomeScreenUI(
                user = userInfo,
                calendarUI = eventBasedCalendar,
                gameEventsOnDate = eventsBySelectedDate,
                isCreateEventButtonVisible = isCreateEventButtonVisible,
            )
        }
            .filterNotNull()
            .onEach { homeScreenUI ->
                uiState.update {
                    UiState.Normal(homeScreenUI)
                }
            }
            .flowOn(ioDispatcher)
            .launchIn(viewModelScope)
    }

    fun onLogOutTap() = viewModelScope.launch {
        if (isLoggingOut.value) return@launch
        isLoggingOut.value = true
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.SignOut)
            navigator.navigateToAuthGraph()
        } else {
            isLoggingOut.value = false
            logger.w { "Failed to sign out" }
        }
    }

    fun onAvatarTap() = viewModelScope.launch {
        navigator.navigate(Destination.ProfileScreen)
    }

    fun onNextMonthTap(nextMonth: LocalDate) = viewModelScope.launch {
        logger.d { "onNextMonthTap: $nextMonth" }

        val nextMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = nextMonth)
        calendarUIState.emit(nextMonthCalendarUI)
    }

    fun onPreviousMonthTap(prevMonth: LocalDate) = viewModelScope.launch {
        logger.d { "onPreviousMonthTap: $prevMonth" }

        val prevMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = prevMonth)
        calendarUIState.emit(prevMonthCalendarUI)
    }

    fun onDateTap(date: Date) = viewModelScope.launch {
        val currentCalendar = calendarUIState.value

        val updatedCalendarUI = calendarUIProvider.updateCalendarBySelectedDate(
            target = currentCalendar,
            selectedDate = date,
        )

        calendarUIState.emit(updatedCalendarUI)
        analyticsProvider.trackEvent(AnalyticsEvent.CalendarDateSelected(date.toString()))
    }

    fun onAddGameEventTap() = viewModelScope.launch {
        val calendarUi = calendarUIState.value
        val selectedDate = calendarUi.dates.firstOrNull { it.enabled && it.isSelected }

        val dayOfMonth = selectedDate?.dayOfMonth
        if (selectedDate == null || dayOfMonth == null) {
            logger.w { "Selected date is null, cannot add game event" }
            return@launch
        }

        val today = todayProvider()
        val selectedLocalDate = LocalDate(
            year = calendarUi.yearMonth.year,
            monthNumber = selectedDate.monthNumber ?: calendarUi.yearMonth.month.number,
            dayOfMonth = dayOfMonth,
        )
        if (selectedLocalDate < today) {
            logger.w { "Selected date is in the past, cannot add game event" }
            return@launch
        }

        analyticsProvider.trackEvent(AnalyticsEvent.CreateEventClicked)
        navigator.navigate(Destination.NewEventScreen(
            selectedDate = selectedDate,
            yearMonth = calendarUi.yearMonth.toString(),
        ))
    }

    fun onJoinGroupDeepLinkRetrieved(inviteGroupId: String?) = viewModelScope.launch {
        if (inviteGroupId == null) return@launch

        logger.d { "Invite group deep link retrieved: $inviteGroupId" }
        inviteGroupIdState.emit(inviteGroupId)
    }

    fun onJoinGroupDialogConfirm() = viewModelScope.launch {
        val inviteGroupId = inviteGroupIdState.value ?: run {
            logger.w { "groupId is null, cannot join group" }
            return@launch
        }
        val currentUser = userInfoState.value ?: run {
            logger.w { "currentUser is null, cannot join group" }
            return@launch
        }

        val isSuccess = profileProvider.joinGroup(
            userId = currentUser.uid, groupId = inviteGroupId,
        )
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.JoinGroup(inviteGroupId))
        } else {
            logger.w { "Failed to join group $inviteGroupId" }
        }
        snackbar.showMessage(
            if (isSuccess) {
                stringProvider.joinedSquad
            } else {
                stringProvider.joinSquadFailed
            }
        )
    }

    fun onJoinGroupDialogDismiss() = viewModelScope.launch {
        confirmInviteDialogState.emit(UiState.Empty)
    }

    fun onEventTap(event: EventUI) = viewModelScope.launch {
        logger.d { "onEventTap: ${event.eventId}" }
        val matchingEvent = eventsState.value.firstOrNull { it.uid == event.eventId }
        val dateText = if (matchingEvent != null && !event.subtitle.isNullOrBlank()) {
            "${matchingEvent.fromDateTime.date}, ${event.subtitle}"
        } else if (matchingEvent != null) {
            matchingEvent.fromDateTime.date.toString()
        } else {
            event.subtitle.orEmpty()
        }
        navigator.navigate(
            Destination.EventDetailsScreen(
                eventId = event.eventId,
                title = event.title,
                date = dateText,
                imageUrl = event.iconUrl,
                isYourEvent = event.isYourEvent,
            )
        )
    }

    fun onAction(action: HomeScreenAction) {
        when (action) {
            is HomeScreenAction.OnDateTap -> onDateTap(action.date)
            is HomeScreenAction.OnNextMonthTap -> onNextMonthTap(action.yearMonth)
            is HomeScreenAction.OnPrevMonthTap -> onPreviousMonthTap(action.yearMonth)
            HomeScreenAction.OnAddGameEventTap -> onAddGameEventTap()
            HomeScreenAction.OnLogOutTap -> onLogOutTap()
            HomeScreenAction.OnAvatarTap -> onAvatarTap()
            HomeScreenAction.OnJoinGroupDialogConfirm -> {
                onJoinGroupDialogConfirm()
                onJoinGroupDialogDismiss()
            }

            HomeScreenAction.OnJoinGroupDialogDismiss -> onJoinGroupDialogDismiss()
            is HomeScreenAction.OnEventTap -> onEventTap(action.event)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
