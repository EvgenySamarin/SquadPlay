package com.eysamarin.squadplay.screens

import androidx.navigation.NavOptionsBuilder
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.game.GameProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.Date
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.NewEventScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.event.NewEventScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewEventScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun collectInitScreenData_emitsUserGroupsInUiState() = runTest(testDispatcher) {
        val group1 = Group(uid = "group-1", title = "Warriors", members = listOf("user-1"))
        val group2 = Group(uid = "group-2", title = "Mages", members = listOf("user-1", "user-2"))
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = listOf(group1, group2),
        )

        val profileProvider = FakeProfileProvider(user = user)
        val viewModel = createViewModel(profileProvider = profileProvider)

        val navArgs = Destination.NewEventScreen(
            selectedDate = Date(dayOfMonth = 10, countEvents = 0, isSelected = true, enabled = true),
            yearMonth = "2026-09-01",
        )
        viewModel.updateSelectedDate(navArgs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertEquals(2, data.userGroups.size)
        assertEquals("group-1", data.userGroups[0].uid)
        assertEquals("group-2", data.userGroups[1].uid)
    }

    @Test
    fun onEventSaveTap_withSpecificGroupId_savesEventWithProvidedGroupId() = runTest(testDispatcher) {
        val group1 = Group(uid = "group-1", title = "Warriors", members = listOf("user-1"))
        val group2 = Group(uid = "group-2", title = "Mages", members = listOf("user-1", "user-2"))
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = listOf(group1, group2),
        )

        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider()
        val navigator = FakeNavigator()
        val analyticsProvider = FakeAnalyticsProvider()

        val viewModel = createViewModel(
            profileProvider = profileProvider,
            eventProvider = eventProvider,
            navigator = navigator,
            analyticsProvider = analyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val from = LocalDateTime(2026, 9, 12, 18, 0)
        val to = LocalDateTime(2026, 9, 12, 20, 0)

        viewModel.onAction(
            NewEventScreenAction.OnEventSaveTap(
                title = "Dota 2 Tournament",
                timeFrom = from,
                timeTo = to,
                eventIconUrl = "https://example.com/icon.png",
                groupId = "group-2",
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = eventProvider.lastSavedEvent
        assertEquals("group-2", saved?.groupId)
        assertEquals("Dota 2 Tournament", saved?.title)
        assertEquals("user-1", saved?.creatorId)
        assertEquals(from, saved?.fromDateTime)
        assertEquals(to, saved?.toDateTime)
        assertEquals("https://example.com/icon.png", saved?.eventIconUrl)
        assertEquals(1, navigator.navigateUpCalls)
        assertTrue(analyticsProvider.trackedEvents.first() is AnalyticsEvent.EventSaved)
    }

    @Test
    fun onEventSaveTap_withBlankGroupId_fallsBackToFirstGroup() = runTest(testDispatcher) {
        val group1 = Group(uid = "group-1", title = "Warriors", members = listOf("user-1"))
        val group2 = Group(uid = "group-2", title = "Mages", members = listOf("user-1", "user-2"))
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = listOf(group1, group2),
        )

        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider()
        val viewModel = createViewModel(profileProvider = profileProvider, eventProvider = eventProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(
            NewEventScreenAction.OnEventSaveTap(
                title = "CS2 match",
                timeFrom = LocalDateTime(2026, 9, 12, 18, 0),
                timeTo = LocalDateTime(2026, 9, 12, 20, 0),
                eventIconUrl = null,
                groupId = "",
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("group-1", eventProvider.lastSavedEvent?.groupId)
    }

    @Test
    fun onEventSaveTap_withNoGroups_showsSnackbarAndDoesNotSave() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Solo",
            email = "solo@test.com",
            photoUrl = null,
            groups = emptyList(),
        )

        val profileProvider = FakeProfileProvider(user = user)
        val eventProvider = FakeEventProvider()
        val snackbar = FakeSnackbarProvider()
        val stringProvider = FakeStringProvider()

        val viewModel = createViewModel(
            profileProvider = profileProvider,
            eventProvider = eventProvider,
            snackbar = snackbar,
            stringProvider = stringProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(
            NewEventScreenAction.OnEventSaveTap(
                title = "Solo Run",
                timeFrom = LocalDateTime(2026, 9, 12, 18, 0),
                timeTo = LocalDateTime(2026, 9, 12, 20, 0),
                eventIconUrl = null,
                groupId = "any-group",
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(eventProvider.lastSavedEvent)
        assertEquals("You have no squads", snackbar.lastMessage)
    }

    @Test
    fun onBackButtonTap_navigatesUp() = runTest(testDispatcher) {
        val navigator = FakeNavigator()
        val viewModel = createViewModel(navigator = navigator)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(NewEventScreenAction.OnBackButtonTap)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, navigator.navigateUpCalls)
    }

    private fun createViewModel(
        navigator: Navigator = FakeNavigator(),
        snackbar: SnackbarProvider = FakeSnackbarProvider(),
        profileProvider: ProfileProvider = FakeProfileProvider(),
        eventProvider: EventProvider = FakeEventProvider(),
        stringProvider: StringProvider = FakeStringProvider(),
        gameProvider: GameProvider = FakeGameProvider(),
        analyticsProvider: AnalyticsProvider = FakeAnalyticsProvider(),
    ): NewEventScreenViewModel {
        return NewEventScreenViewModel(
            navigator = navigator,
            snackbar = snackbar,
            profileProvider = profileProvider,
            eventProvider = eventProvider,
            stringProvider = stringProvider,
            gameProvider = gameProvider,
            analyticsProvider = analyticsProvider,
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

    private class FakeProfileProvider(
        var user: User? = null
    ) : ProfileProvider {
        private val userFlow = MutableStateFlow(user)
        override fun getUserInfoFlow(): Flow<User?> = userFlow
        override fun createNewInviteLink(inviteGroupId: String): String = ""
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null
        override suspend fun createNewUserGroup(userId: String, title: String): String = ""
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = emptyFlow()
    }

    private class FakeEventProvider : EventProvider {
        var lastSavedEvent: Event? = null
        override suspend fun saveEventData(event: Event): Boolean {
            lastSavedEvent = event
            return true
        }
        override fun getEventsFlow(groupId: String): Flow<List<Event>> = emptyFlow()
        override suspend fun deleteEvent(eventId: String): Boolean = true
    }

    private class FakeSnackbarProvider : SnackbarProvider {
        var lastMessage: String? = null
        override val messagesChannel: Flow<String> = emptyFlow()
        override suspend fun showMessage(message: String) {
            lastMessage = message
        }
    }

    private class FakeStringProvider : StringProvider {
        override val cannotSignText: String = ""
        override val alreadyInSquad: String = ""
        override fun squadNotFound(groupId: String): String = ""
        override fun wantToJoinSquad(groupTitle: String): String = ""
        override fun fromToDate(fromDate: String, toDate: String): String = ""
        override val youHaveNoSquad: String = "You have no squads"
        override val eventSaved: String = "Event saved"
        override val eventSaveFailed: String = "Event save failed"
        override val joinedSquad: String = ""
        override val joinSquadFailed: String = ""
    }

    private class FakeGameProvider : GameProvider {
        override suspend fun getGameThumbnailUrl(gameTitle: String): String? = null
    }

    private class FakeAnalyticsProvider : AnalyticsProvider {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        override fun trackEvent(event: AnalyticsEvent) {
            trackedEvents.add(event)
        }
        override fun trackScreenView(screenName: String) {}
        override fun setUserId(userId: String?) {}
    }

    private class FakeAppLogger : AppLogger {
        override fun d(tag: String?, message: () -> String) {}
        override fun i(tag: String?, message: () -> String) {}
        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    }
}
