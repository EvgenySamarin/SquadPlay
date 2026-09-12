package com.eysamarin.squadplay.screens

import androidx.navigation.NavOptionsBuilder
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
import com.eysamarin.squadplay.models.EventDetailsScreenAction
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.EventUI
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.DefaultDeepLinkManager
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.event.EventDetailsScreenViewModel
import com.eysamarin.squadplay.screens.main.HomeScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import com.eysamarin.squadplay.domain.calendar.CalendarUIProviderImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenEventNavigationTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        HomeScreenViewModel.defaultIoDispatcher = testDispatcher
        HomeScreenViewModel.defaultTodayProvider = { LocalDate(2026, 9, 9) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        HomeScreenViewModel.defaultIoDispatcher = Dispatchers.IO
        HomeScreenViewModel.defaultTodayProvider = {
            java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
        }
    }

    @Test
    fun homeScreenViewModel_onEventTap_navigatesToEventDetailsScreen() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val viewModel = HomeScreenViewModel(
            navigator = fakeNavigator,
            authProvider = FakeAuthProvider(),
            calendarUIProvider = FakeCalendarUIProvider(),
            eventProvider = FakeEventProvider(),
            snackbar = FakeSnackbarProvider(),
            profileProvider = FakeProfileProvider(),
            stringProvider = FakeStringProvider(),
            deepLinkManager = DefaultDeepLinkManager(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        val eventUI = EventUI(
            eventId = "test-event-1",
            title = "Apex Legends",
            subtitle = "from 18:00 to 20:00",
            iconUrl = "https://example.com/image.jpg",
            isYourEvent = true,
        )

        viewModel.onAction(HomeScreenAction.OnEventTap(eventUI))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigatedDestinations.size)
        val destination = fakeNavigator.navigatedDestinations.first()
        assertTrue(destination is Destination.EventDetailsScreen)
        val eventDetailsScreen = destination as Destination.EventDetailsScreen
        assertEquals("test-event-1", eventDetailsScreen.eventId)
        assertEquals("Apex Legends", eventDetailsScreen.title)
        assertEquals("from 18:00 to 20:00", eventDetailsScreen.date)
        assertEquals("https://example.com/image.jpg", eventDetailsScreen.imageUrl)
        assertTrue(eventDetailsScreen.isYourEvent)
    }

    @Test
    fun eventDetailsScreenViewModel_onBackButtonTap_navigatesUp() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val viewModel = EventDetailsScreenViewModel(
            navigator = fakeNavigator,
            eventProvider = FakeEventProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        viewModel.onAction(EventDetailsScreenAction.OnBackButtonTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigateUpCalls)
    }

    @Test
    fun eventDetailsScreenViewModel_deleteFlow_showsConfirmationAndDeletesOnConfirm() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val fakeEventProvider = FakeEventProvider()
        val viewModel = EventDetailsScreenViewModel(
            navigator = fakeNavigator,
            eventProvider = fakeEventProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        viewModel.initData(
            Destination.EventDetailsScreen(
                eventId = "event-to-delete",
                title = "Apex Legends",
                date = "18:00 - 20:00",
                imageUrl = null,
                isYourEvent = true,
            )
        )

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.onAction(EventDetailsScreenAction.OnDeleteTap)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.onAction(EventDetailsScreenAction.OnDismissDeleteDialog)
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.onAction(EventDetailsScreenAction.OnDeleteTap)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.onAction(EventDetailsScreenAction.OnConfirmDeleteTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        assertEquals("event-to-delete", fakeEventProvider.deletedEventId)
        assertEquals(1, fakeNavigator.navigateUpCalls)
    }

    @Test
    fun homeScreenViewModel_pastDateSelected_hidesButtonAndBlocksNavigation() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val pastDate = Date(dayOfMonth = 8, monthNumber = 9, countEvents = 0, isSelected = true, enabled = true)
        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(pastDate))
        val viewModel = createHomeScreenViewModel(fakeNavigator, calendarProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertFalse(data.isCreateEventButtonVisible)

        viewModel.onAction(HomeScreenAction.OnAddGameEventTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeNavigator.navigatedDestinations.isEmpty())
    }

    @Test
    fun homeScreenViewModel_todaySelected_showsButtonAndAllowsNavigation() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val todayDate = Date(dayOfMonth = 9, monthNumber = 9, countEvents = 0, isSelected = true, enabled = true)
        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(todayDate))
        val viewModel = createHomeScreenViewModel(fakeNavigator, calendarProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertTrue(data.isCreateEventButtonVisible)

        viewModel.onAction(HomeScreenAction.OnAddGameEventTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigatedDestinations.size)
        assertTrue(fakeNavigator.navigatedDestinations.first() is Destination.NewEventScreen)
    }

    @Test
    fun homeScreenViewModel_futureDateSelected_showsButtonAndAllowsNavigation() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val futureDate = Date(dayOfMonth = 10, monthNumber = 9, countEvents = 0, isSelected = true, enabled = true)
        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(futureDate))
        val viewModel = createHomeScreenViewModel(fakeNavigator, calendarProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertTrue(data.isCreateEventButtonVisible)

        viewModel.onAction(HomeScreenAction.OnAddGameEventTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigatedDestinations.size)
        assertTrue(fakeNavigator.navigatedDestinations.first() is Destination.NewEventScreen)
    }

    @Test
    fun homeScreenViewModel_onDateTap_updatesCreateEventButtonVisibility() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val todayDate = Date(dayOfMonth = 9, monthNumber = 9, countEvents = 0, isSelected = true, enabled = true)
        val pastDate = Date(dayOfMonth = 8, monthNumber = 9, countEvents = 0, isSelected = false, enabled = true)
        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(todayDate, pastDate))
        val viewModel = createHomeScreenViewModel(fakeNavigator, calendarProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        var data = (viewModel.uiState.value as UiState.Normal).data
        assertTrue(data.isCreateEventButtonVisible)

        // Tap past date
        viewModel.onAction(HomeScreenAction.OnDateTap(pastDate))
        testDispatcher.scheduler.advanceUntilIdle()

        data = (viewModel.uiState.value as UiState.Normal).data
        assertFalse(data.isCreateEventButtonVisible)

        // Tap today
        viewModel.onAction(HomeScreenAction.OnDateTap(todayDate))
        testDispatcher.scheduler.advanceUntilIdle()

        data = (viewModel.uiState.value as UiState.Normal).data
        assertTrue(data.isCreateEventButtonVisible)
    }

    @Test
    fun homeScreenViewModel_eventsOnSelectedDate_filtersByYear_doesNotShowEventsFromOtherYears() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val user = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = listOf(Group(uid = "group-1", title = "Squad 1", members = listOf("user-1")))
        )
        val event2026 = Event(
            uid = "event-2026",
            creatorId = "user-1",
            groupId = "group-1",
            title = "Apex Tournament",
            eventIconUrl = "https://example.com/apex.png",
            fromDateTime = LocalDateTime(2026, 9, 9, 14, 0),
            toDateTime = LocalDateTime(2026, 9, 9, 16, 0),
        )
        val date2026 = Date(dayOfMonth = 9, monthNumber = 9, year = 2026, countEvents = 1, isSelected = true, enabled = true)
        val date2027 = Date(dayOfMonth = 9, monthNumber = 9, year = 2027, countEvents = 0, isSelected = false, enabled = true)

        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(date2026, date2027))
        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider(events = listOf(event2026))

        val viewModel = createHomeScreenViewModel(
            fakeNavigator = fakeNavigator,
            calendarProvider = calendarProvider,
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        var data = (viewModel.uiState.value as UiState.Normal).data
        assertEquals(1, data.gameEventsOnDate.size)
        assertEquals("event-2026", data.gameEventsOnDate.first().eventId)
        assertEquals("Apex Tournament", data.gameEventsOnDate.first().title)
        assertEquals("from 14:00 to 16:00", data.gameEventsOnDate.first().subtitle)

        // Navigate / select date in next year (2027)
        viewModel.onAction(HomeScreenAction.OnDateTap(date2027))
        testDispatcher.scheduler.advanceUntilIdle()

        data = (viewModel.uiState.value as UiState.Normal).data
        assertTrue(data.gameEventsOnDate.isEmpty())
    }

    @Test
    fun homeScreenViewModel_eventsOnSelectedDate_whenNoDateSelected_returnsEmptyList() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val user = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = listOf(Group(uid = "group-1", title = "Squad 1", members = listOf("user-1")))
        )
        val event2026 = Event(
            uid = "event-2026",
            creatorId = "user-1",
            groupId = "group-1",
            title = "Apex Tournament",
            fromDateTime = LocalDateTime(2026, 9, 9, 14, 0),
            toDateTime = LocalDateTime(2026, 9, 9, 16, 0),
        )
        val unselectedDate = Date(dayOfMonth = 9, monthNumber = 9, year = 2026, countEvents = 1, isSelected = false, enabled = true)

        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(unselectedDate))
        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider(events = listOf(event2026))

        val viewModel = createHomeScreenViewModel(
            fakeNavigator = fakeNavigator,
            calendarProvider = calendarProvider,
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val data = (viewModel.uiState.value as UiState.Normal).data
        assertTrue(data.gameEventsOnDate.isEmpty())
    }

    @Test
    fun calendarUIProviderImpl_mergedCalendarWithEvents_matchesEventsByYear() {
        val provider = CalendarUIProviderImpl()
        val event2026 = Event(
            uid = "event-1",
            creatorId = "user-1",
            groupId = "group-1",
            title = "Match",
            fromDateTime = LocalDateTime(2026, 9, 9, 10, 0),
            toDateTime = LocalDateTime(2026, 9, 9, 12, 0),
        )
        val calendar2027 = CalendarUI(
            daysOfWeek = emptyList(),
            yearMonth = LocalDate(2027, 9, 1),
            dates = listOf(
                Date(dayOfMonth = 9, monthNumber = 9, year = 2027, countEvents = 0, isSelected = false, enabled = true)
            )
        )
        val merged2027 = provider.mergedCalendarWithEvents(calendar2027, listOf(event2026), "user-1")
        assertEquals(0, merged2027.dates.first().countEvents)
        assertFalse(merged2027.dates.first().hasUserEvents)

        val calendar2026 = CalendarUI(
            daysOfWeek = emptyList(),
            yearMonth = LocalDate(2026, 9, 1),
            dates = listOf(
                Date(dayOfMonth = 9, monthNumber = 9, year = 2026, countEvents = 0, isSelected = false, enabled = true)
            )
        )
        val merged2026 = provider.mergedCalendarWithEvents(calendar2026, listOf(event2026), "user-1")
        assertEquals(1, merged2026.dates.first().countEvents)
        assertTrue(merged2026.dates.first().hasUserEvents)
    }

    @Test
    fun homeScreenViewModel_fetchesEventsForAllUserGroups() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = listOf(
                Group(uid = "group-alpha", title = "Alpha Squad", members = listOf("user-1")),
                Group(uid = "group-beta", title = "Beta Squad", members = listOf("user-1")),
            )
        )
        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider()

        createHomeScreenViewModel(
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(setOf("group-alpha", "group-beta"), eventProvider.requestedGroupIds)
    }

    @Test
    fun homeScreenViewModel_eventsOnSelectedDate_populatesGroupTitleFromUserGroups() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = listOf(
                Group(uid = "group-alpha", title = "Alpha Squad", members = listOf("user-1")),
                Group(uid = "group-beta", title = "Beta Squad", members = listOf("user-1")),
            )
        )
        val selectedDate = Date(dayOfMonth = 9, monthNumber = 9, year = 2026, countEvents = 2, isSelected = true, enabled = true)
        val event1 = Event(
            uid = "event-1",
            creatorId = "user-2",
            groupId = "group-alpha",
            title = "Alpha Scrim",
            fromDateTime = LocalDateTime(2026, 9, 9, 10, 0),
            toDateTime = LocalDateTime(2026, 9, 9, 12, 0),
        )
        val event2 = Event(
            uid = "event-2",
            creatorId = "user-1",
            groupId = "group-beta",
            title = "Beta Tournament",
            fromDateTime = LocalDateTime(2026, 9, 9, 14, 0),
            toDateTime = LocalDateTime(2026, 9, 9, 16, 0),
        )

        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(selectedDate))
        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider(events = listOf(event1, event2))

        val viewModel = createHomeScreenViewModel(
            calendarProvider = calendarProvider,
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val data = (viewModel.uiState.value as UiState.Normal).data
        assertEquals(2, data.gameEventsOnDate.size)

        val item1 = data.gameEventsOnDate.first { it.eventId == "event-1" }
        assertEquals("Alpha Squad", item1.groupTitle)
        assertEquals("Alpha Scrim", item1.title)
        assertFalse(item1.isYourEvent)

        val item2 = data.gameEventsOnDate.first { it.eventId == "event-2" }
        assertEquals("Beta Squad", item2.groupTitle)
        assertEquals("Beta Tournament", item2.title)
        assertTrue(item2.isYourEvent)
    }

    @Test
    fun homeScreenViewModel_whenUserHasNoGroups_passesEmptyGroupIdsList() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = emptyList()
        )
        val selectedDate = Date(dayOfMonth = 9, monthNumber = 9, year = 2026, countEvents = 0, isSelected = true, enabled = true)

        val calendarProvider = FakeCalendarUIProvider(initialDates = listOf(selectedDate))
        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider(events = emptyList())

        val viewModel = createHomeScreenViewModel(
            calendarProvider = calendarProvider,
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptySet<String>(), eventProvider.requestedGroupIds)
        val data = (viewModel.uiState.value as UiState.Normal).data
        assertTrue(data.gameEventsOnDate.isEmpty())
    }

    @Test
    fun homeScreenViewModel_whenGroupOrderChanges_doesNotRefetchEventsDueToSetEquality() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow(
            User(
                uid = "user-1",
                username = "tester",
                email = "tester@test.com",
                photoUrl = null,
                groups = listOf(
                    Group(uid = "group-alpha", title = "Alpha Squad", members = listOf("user-1")),
                    Group(uid = "group-beta", title = "Beta Squad", members = listOf("user-1")),
                )
            )
        )
        val profileProvider = FakeProfileProvider(userFlow = userFlow)
        val eventProvider = FakeEventProvider()

        createHomeScreenViewModel(
            profileProvider = profileProvider,
            eventProvider = eventProvider,
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, eventProvider.getEventsFlowCallCount)

        userFlow.value = User(
            uid = "user-1",
            username = "tester",
            email = "tester@test.com",
            photoUrl = null,
            groups = listOf(
                Group(uid = "group-beta", title = "Beta Squad", members = listOf("user-1")),
                Group(uid = "group-alpha", title = "Alpha Squad", members = listOf("user-1")),
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, eventProvider.getEventsFlowCallCount)
    }

    private fun createHomeScreenViewModel(
        fakeNavigator: FakeNavigator = FakeNavigator(),
        calendarProvider: FakeCalendarUIProvider = FakeCalendarUIProvider(),
        profileProvider: FakeProfileProvider = FakeProfileProvider(),
        eventProvider: FakeEventProvider = FakeEventProvider(),
    ): HomeScreenViewModel {
        return HomeScreenViewModel(
            navigator = fakeNavigator,
            authProvider = FakeAuthProvider(),
            calendarUIProvider = calendarProvider,
            eventProvider = eventProvider,
            snackbar = FakeSnackbarProvider(),
            profileProvider = profileProvider,
            stringProvider = FakeStringProvider(),
            deepLinkManager = DefaultDeepLinkManager(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
    }

    private class FakeNavigator : Navigator {
        val navigatedDestinations = mutableListOf<Destination>()
        var navigateUpCalls = 0
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination, navOptions: NavOptionsBuilder.() -> Unit) {
            navigatedDestinations.add(destination)
        }
        override suspend fun navigateToHomeGraph() {}
        override suspend fun navigateToAuthGraph() {}
        override suspend fun navigateUp() {
            navigateUpCalls++
        }
    }

    private class FakeAuthProvider : AuthProvider {
        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signOut(): Boolean = true
        override suspend fun isUserExists(): Boolean = true
    }

    private class FakeProfileProvider(
        var user: User? = User(uid = "user1", username = "tester", email = "test@example.com", photoUrl = null, groups = emptyList()),
        val userFlow: Flow<User?>? = null,
    ) : ProfileProvider {
        override fun getUserInfoFlow(): Flow<User?> = userFlow ?: flowOf(user)
        override fun createNewInviteLink(inviteGroupId: String): String = ""
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null
        override suspend fun createNewUserGroup(userId: String, title: String): String = ""
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(emptyList())
    }

    private class FakeCalendarUIProvider(
        var initialDates: List<Date> = emptyList(),
    ) : CalendarUIProvider {
        override fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI {
            return CalendarUI(daysOfWeek = emptyList(), yearMonth = yearMonth, dates = initialDates)
        }
        override fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI {
            return target.copy(
                dates = target.dates.map {
                    it.copy(
                        isSelected = it.dayOfMonth == selectedDate.dayOfMonth
                                && it.monthNumber == selectedDate.monthNumber
                                && (selectedDate.year == null || it.year == selectedDate.year)
                    )
                }
            )
        }
        override fun mergedCalendarWithEvents(
            calendar: CalendarUI,
            events: List<Event>,
            currentUserId: String
        ): CalendarUI = calendar
    }

    private class FakeEventProvider(
        var events: List<Event> = emptyList()
    ) : EventProvider {
        var deletedEventId: String? = null
        var requestedGroupIds: Set<String>? = null
        var getEventsFlowCallCount = 0
        override suspend fun saveEventData(event: Event): Boolean = true
        override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> {
            getEventsFlowCallCount++
            requestedGroupIds = groupIds
            return flowOf(events)
        }
        override suspend fun deleteEvent(eventId: String): Boolean {
            deletedEventId = eventId
            return true
        }
        override suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus) {}
    }

    private class FakeSnackbarProvider : SnackbarProvider {
        override val messagesChannel: Flow<String> = emptyFlow()
        override suspend fun showMessage(message: String) {}
    }

    private class FakeStringProvider : StringProvider {
        override val cannotSignText: String = ""
        override val alreadyInSquad: String = ""
        override fun squadNotFound(groupId: String): String = ""
        override fun wantToJoinSquad(groupTitle: String): String = ""
        override fun fromToDate(fromDate: String, toDate: String): String = "from $fromDate to $toDate"
        override val youHaveNoSquad: String = ""
        override val eventSaved: String = ""
        override val eventSaveFailed: String = ""
        override val joinedSquad: String = ""
        override val joinSquadFailed: String = ""
    }

    private class FakeAnalyticsProvider : AnalyticsProvider {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        val trackedScreens = mutableListOf<String>()
        override fun trackEvent(event: AnalyticsEvent) {
            trackedEvents.add(event)
        }
        override fun trackScreenView(screenName: String) {
            trackedScreens.add(screenName)
        }
        override fun setUserId(userId: String?) {}
    }

    private class FakeAppLogger : AppLogger {
        override fun d(tag: String?, message: () -> String) {}
        override fun i(tag: String?, message: () -> String) {}
        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    }
}
