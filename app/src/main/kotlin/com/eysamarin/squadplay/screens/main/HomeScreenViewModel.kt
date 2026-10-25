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
import com.eysamarin.squadplay.models.AppErrorException
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.EventMemberUI
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.EventUI
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.HomeEventFilter
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.HomeScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlin.time.Duration.Companion.milliseconds

class HomeScreenViewModel(
    private val navigator: Navigator,
    private val calendarUIProvider: CalendarUIProvider,
    private val eventProvider: EventProvider,
    private val authProvider: AuthProvider,
    private val profileProvider: ProfileProvider,
    private val stringProvider: StringProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    companion object {
        const val DEFAULT_LOADING_TIMEOUT_MS: Long = 10_000L
        internal var defaultIoDispatcher: CoroutineDispatcher = Dispatchers.IO
        internal var defaultTodayProvider: () -> LocalDate = {
            java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
        }
        internal var defaultNowProvider: () -> LocalDateTime = {
            java.time.LocalDateTime.now().let {
                LocalDateTime(it.year, it.monthValue, it.dayOfMonth, it.hour, it.minute, it.second)
            }
        }
    }

    internal var ioDispatcher: CoroutineDispatcher = defaultIoDispatcher
    internal var todayProvider: () -> LocalDate = defaultTodayProvider
    internal var nowProvider: () -> LocalDateTime = defaultNowProvider
    internal var loadingTimeoutMillis: Long = DEFAULT_LOADING_TIMEOUT_MS

    val uiState: StateFlow<UiState<HomeScreenUI>>
        field = MutableStateFlow<UiState<HomeScreenUI>>(UiState.Loading)

    val isLoggingOut: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isTimeoutDialogVisible: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private var dataCollectionJob: Job? = null

    private val userInfoState = MutableStateFlow<User?>(null)
    private val eventsState = MutableStateFlow<List<Event>>(emptyList())
    private val groupSectionsState = MutableStateFlow<List<UserGroupSection>>(emptyList())
    private val activeFilterState = MutableStateFlow(HomeEventFilter.ALL)
    private val calendarUIState = MutableStateFlow(
        calendarUIProvider.provideCalendarUIBy(yearMonth = todayProvider().run { LocalDate(year, month.number, 1) })
    )

    init {
        collectUiStateData()
    }

    private fun resetStates() {
        userInfoState.value = null
        eventsState.value = emptyList()
        groupSectionsState.value = emptyList()
        activeFilterState.value = HomeEventFilter.ALL
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectUiStateData() {
        dataCollectionJob?.cancel()
        isLoggingOut.value = false
        isTimeoutDialogVisible.value = false
        resetStates()

        dataCollectionJob = viewModelScope.launch {
            launch {
                try {
                    withTimeout(loadingTimeoutMillis.milliseconds) {
                        uiState.first { it is UiState.Normal }
                    }
                } catch (_: TimeoutCancellationException) {
                    if (uiState.value is UiState.Loading) {
                        logger.w { "HomeScreen data loading timed out after ${loadingTimeoutMillis}ms" }
                        isTimeoutDialogVisible.value = true
                    }
                }
            }

            launch {
                profileProvider.getUserInfoFlow()
                    .catch { throwable ->
                        logger.w(throwable = throwable) { "Failed to fetch user info flow: ${throwable.message}" }
                        isTimeoutDialogVisible.value = true
                    }
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
                    .map { user -> user.groups }
                    .distinctUntilChanged { old, new -> old.map { it.uid }.toSet() == new.map { it.uid }.toSet() }
                    .flatMapLatest { groups ->
                        val groupIds = groups.map { it.uid }.toSet()
                        val eventsFlow = eventProvider.getEventsFlow(groupIds)
                            .catch {
                                if (it is AppErrorException) throw it

                                logger.w(throwable = it) { "Failed to fetch events flow: ${it.message}" }
                                isTimeoutDialogVisible.value = true
                                emit(emptyList())
                            }
                        val sectionsFlow = if (groups.isNotEmpty()) {
                            profileProvider.getGroupsMembersInfoFlow(groups)
                        } else {
                            flowOf(emptyList())
                        }
                        combine(eventsFlow, sectionsFlow) { events, sections ->
                            events to sections
                        }
                    }
                    .collect { (events, sections) ->
                        logger.d { "Events & Sections received: ${events.size} events, ${sections.size} sections" }
                        eventsState.emit(events)
                        groupSectionsState.emit(sections)
                    }
            }

            launch {
                combine(
                    userInfoState,
                    calendarUIState,
                    eventsState,
                    groupSectionsState,
                    activeFilterState,
                ) { userInfo, calendar, events, groupSections, activeFilter ->
                    userInfo ?: return@combine null

                    val eventBasedCalendar = calendarUIProvider.mergedCalendarWithEvents(
                        calendar = calendar,
                        events = events,
                        currentUserId = userInfo.uid,
                        groupMembers = groupSections.associate { it.groupId to it.members },
                    )

                    val selectedDate = eventBasedCalendar.dates.firstOrNull { it.isSelected }
                    val eventsBySelectedDate = getEventsBySelectedDate(
                        events = events,
                        selectedDate = selectedDate,
                        calendarYearMonth = eventBasedCalendar.yearMonth,
                        activeFilter = activeFilter,
                        currentUser = userInfo,
                        userGroups = userInfo.groups,
                        groupSections = groupSections,
                    )
                    val today = todayProvider()
                    val dayOfMonth = selectedDate?.dayOfMonth
                    val selectedLocalDate = if (selectedDate != null && dayOfMonth != null && selectedDate.enabled) {
                        LocalDate(
                            year = selectedDate.year ?: eventBasedCalendar.yearMonth.year,
                            month = selectedDate.monthNumber ?: eventBasedCalendar.yearMonth.month.number,
                            day = dayOfMonth
                        )
                    } else {
                        null
                    }
                    val isCreateEventButtonVisible = userInfo.groups.isNotEmpty() &&
                        selectedLocalDate != null &&
                        selectedLocalDate >= today
                    val isTodaySelected = selectedLocalDate == today

                    HomeScreenUI(
                        user = userInfo,
                        calendarUI = eventBasedCalendar,
                        gameEventsOnDate = eventsBySelectedDate,
                        isCreateEventButtonVisible = isCreateEventButtonVisible,
                        activeFilter = activeFilter,
                        isTodaySelected = isTodaySelected,
                        todayDate = today,
                    )
                }
                    .filterNotNull()
                    .flowOn(ioDispatcher)
                    .collect { homeScreenUI ->
                        isTimeoutDialogVisible.value = false
                        uiState.update {
                            UiState.Normal(homeScreenUI)
                        }
                    }
            }
        }
    }

    fun onLogOutTap() = viewModelScope.launch {
        if (isLoggingOut.value) return@launch
        isLoggingOut.value = true
        val isSuccess = authProvider.signOut()
        isLoggingOut.value = false
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.SignOut)
            navigator.navigateToAuthGraph()
        } else {
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
        logger.d { "onDateTap: $date" }
    }

    fun onAllMonthTap() = viewModelScope.launch {
        logger.d { "onAllMonthTap" }
        activeFilterState.value = HomeEventFilter.ALL
        val currentCalendar = calendarUIState.value
        val updatedCalendarUI = calendarUIProvider.clearSelectedDate(currentCalendar)
        calendarUIState.emit(updatedCalendarUI)
    }

    fun onTodayTap() = viewModelScope.launch {
        logger.d { "onTodayTap" }
        val today = todayProvider()
        val todayYearMonth = LocalDate(today.year, today.month.number, 1)
        val currentCalendar = calendarUIState.value
        val baseCalendar = if (currentCalendar.yearMonth.year == today.year &&
            currentCalendar.yearMonth.month.number == today.month.number
        ) {
            currentCalendar
        } else {
            calendarUIProvider.provideCalendarUIBy(yearMonth = todayYearMonth)
        }
        val todayDate = baseCalendar.dates.firstOrNull {
            it.enabled &&
                it.dayOfMonth == today.day &&
                it.monthNumber == today.month.number &&
                (it.year ?: baseCalendar.yearMonth.year) == today.year
        } ?: Date(
            dayOfMonth = today.day,
            monthNumber = today.month.number,
            year = today.year,
            countEvents = 0,
            isSelected = true,
            enabled = true,
        )
        val updatedCalendarUI = calendarUIProvider.updateCalendarBySelectedDate(
            target = baseCalendar,
            selectedDate = todayDate,
        )
        calendarUIState.emit(updatedCalendarUI)
    }

    fun onFilterTap(filter: HomeEventFilter) = viewModelScope.launch {
        logger.d { "onFilterTap: $filter" }
        activeFilterState.update { current ->
            if (current == filter) HomeEventFilter.ALL else filter
        }
    }

    fun onAddGameEventTap() = viewModelScope.launch {
        logger.d { "onAddGameEventTap" }
        val currentUser = userInfoState.value
        if (currentUser == null || currentUser.groups.isEmpty()) {
            logger.w { "User has no groups, cannot add game event" }
            return@launch
        }
        val calendarUi = calendarUIState.value
        val selectedDate = calendarUi.dates.firstOrNull { it.enabled && it.isSelected }

        val dayOfMonth = selectedDate?.dayOfMonth
        if (selectedDate == null || dayOfMonth == null) {
            logger.w { "Selected date is null, cannot add game event" }
            return@launch
        }

        val today = todayProvider()
        val selectedLocalDate = LocalDate(
            year = selectedDate.year ?: calendarUi.yearMonth.year,
            month = selectedDate.monthNumber ?: calendarUi.yearMonth.month.number,
            day = dayOfMonth
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

    fun onEventTap(event: EventUI) = viewModelScope.launch {
        logger.d { "onEventTap: ${event.eventId}" }
        val matchingEvent = eventsState.value.firstOrNull { it.uid == event.eventId }
        val dateText = if (matchingEvent != null && !event.subtitle.isNullOrBlank()) {
            "${matchingEvent.fromDateTime.date}, ${event.subtitle}"
        } else {
            matchingEvent?.fromDateTime?.date?.toString() ?: event.subtitle.orEmpty()
        }
        val targetGroupId = event.groupId ?: matchingEvent?.groupId.orEmpty()
        val userStatus = event.members.firstOrNull()?.status ?: EventResponseStatus.NOT_SET
        val isObsolete = matchingEvent?.isObsolete(nowProvider()) ?: false
        navigator.navigate(
            Destination.EventDetailsScreen(
                eventId = event.eventId,
                title = event.title,
                date = dateText,
                imageUrl = event.iconUrl,
                isYourEvent = event.isYourEvent,
                userStatus = userStatus,
                groupId = targetGroupId,
                isObsolete = isObsolete,
            )
        )
    }

    fun onRetryLoadingTap() {
        logger.d { "Retrying data load after timeout" }
        uiState.update { UiState.Loading }
        collectUiStateData()
    }

    fun onDismissTimeoutDialog() {
        logger.d { "Dismissing timeout dialog" }
        isTimeoutDialogVisible.value = false
    }

    fun onAction(action: HomeScreenAction) {
        when (action) {
            is HomeScreenAction.OnDateTap -> onDateTap(action.date)
            is HomeScreenAction.OnNextMonthTap -> onNextMonthTap(action.yearMonth)
            is HomeScreenAction.OnPrevMonthTap -> onPreviousMonthTap(action.yearMonth)
            HomeScreenAction.OnAllMonthTap -> onAllMonthTap()
            HomeScreenAction.OnTodayTap -> onTodayTap()
            is HomeScreenAction.OnFilterTap -> onFilterTap(action.filter)
            HomeScreenAction.OnAddGameEventTap -> onAddGameEventTap()
            HomeScreenAction.OnLogOutTap -> onLogOutTap()
            HomeScreenAction.OnAvatarTap -> onAvatarTap()
            is HomeScreenAction.OnEventTap -> onEventTap(action.event)
            HomeScreenAction.OnRetryLoadingTap -> onRetryLoadingTap()
            HomeScreenAction.OnDismissTimeoutDialog -> onDismissTimeoutDialog()
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val h = if (hour < 10) "0$hour" else hour.toString()
        val m = if (minute < 10) "0$minute" else minute.toString()
        return "$h:$m"
    }

    private fun getEventsBySelectedDate(
        events: List<Event>,
        selectedDate: Date?,
        calendarYearMonth: LocalDate,
        activeFilter: HomeEventFilter,
        currentUser: User,
        userGroups: List<Group> = emptyList(),
        groupSections: List<UserGroupSection> = emptyList(),
    ): List<EventUI> {
        if (events.isEmpty()) return emptyList()

        val matchingEvents = if (selectedDate != null) {
            val selectedDay = selectedDate.dayOfMonth ?: return emptyList()
            val selectedMonth = selectedDate.monthNumber ?: return emptyList()
            val selectedYear = selectedDate.year ?: calendarYearMonth.year
            events.filter { event ->
                val fromDate = event.fromDateTime
                fromDate.day == selectedDay &&
                    fromDate.month.number == selectedMonth &&
                    fromDate.year == selectedYear
            }
        } else {
            events.filter { event ->
                val fromDate = event.fromDateTime
                fromDate.month.number == calendarYearMonth.month.number &&
                    fromDate.year == calendarYearMonth.year
            }
        }.sortedBy { it.fromDateTime }

        val groupsById = userGroups.associateBy { it.uid }
        val sectionsByGroupId = groupSections.associateBy { it.groupId }

        return matchingEvents.map { event ->
            val fromDate = event.fromDateTime
            val groupSection = sectionsByGroupId[event.groupId]

            // Current user is the first member
            val userMember = EventMemberUI(
                uid = currentUser.uid,
                username = currentUser.username,
                photoUrl = currentUser.photoUrl,
                status = event.getStatusForUser(currentUser.uid)
            )

            // Other group members
            val otherMembers = groupSection?.members
                ?.filter { it.uid != currentUser.uid }
                ?.map { friend ->
                    EventMemberUI(
                        uid = friend.uid,
                        username = friend.username,
                        photoUrl = friend.photoUrl,
                        status = event.getStatusForUser(friend.uid)
                    )
                }.orEmpty()

            val allMembers = listOf(userMember) + otherMembers

            EventUI(
                eventId = event.uid,
                title = event.title,
                groupTitle = groupsById[event.groupId]?.title,
                groupId = event.groupId,
                subtitle = stringProvider.fromToDate(
                    fromDate = formatTime(fromDate.hour, fromDate.minute),
                    toDate = formatTime(event.toDateTime.hour, event.toDateTime.minute),
                ),
                iconUrl = event.eventIconUrl,
                isYourEvent = event.creatorId == currentUser.uid,
                members = allMembers,
            )
        }.filter { eventUi ->
            when (activeFilter) {
                HomeEventFilter.ALL -> true
                HomeEventFilter.SQUAD_EVENTS -> !eventUi.isYourEvent
                HomeEventFilter.MY_MATCHES -> eventUi.isYourEvent
                HomeEventFilter.CONFIRMED -> eventUi.members.isNotEmpty() &&
                    eventUi.members.all { it.status == EventResponseStatus.ACCEPTED }
            }
        }
    }
}
