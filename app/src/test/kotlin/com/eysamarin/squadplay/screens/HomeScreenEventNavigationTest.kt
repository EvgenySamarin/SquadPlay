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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
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

    private fun createHomeScreenViewModel(
        fakeNavigator: FakeNavigator,
        calendarProvider: FakeCalendarUIProvider,
    ): HomeScreenViewModel {
        return HomeScreenViewModel(
            navigator = fakeNavigator,
            authProvider = FakeAuthProvider(),
            calendarUIProvider = calendarProvider,
            eventProvider = FakeEventProvider(),
            snackbar = FakeSnackbarProvider(),
            profileProvider = FakeProfileProvider(),
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

    private class FakeProfileProvider : ProfileProvider {
        override fun getUserInfoFlow(): Flow<User?> = flowOf(
            User(uid = "user1", username = "tester", email = "test@example.com", photoUrl = null, groups = emptyList())
        )
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
                    it.copy(isSelected = it.dayOfMonth == selectedDate.dayOfMonth && it.monthNumber == selectedDate.monthNumber)
                }
            )
        }
        override fun mergedCalendarWithEvents(
            calendar: CalendarUI,
            events: List<Event>,
            currentUserId: String
        ): CalendarUI = calendar
    }

    private class FakeEventProvider : EventProvider {
        var deletedEventId: String? = null
        override suspend fun saveEventData(event: Event): Boolean = true
        override fun getEventsFlow(groupId: String): Flow<List<Event>> = flowOf(emptyList())
        override suspend fun deleteEvent(eventId: String): Boolean {
            deletedEventId = eventId
            return true
        }
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
        override fun fromToDate(fromDate: String, toDate: String): String = ""
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
