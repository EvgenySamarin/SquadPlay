package com.eysamarin.squadplay.screens

import androidx.navigation.NavOptionsBuilder
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.data.entity.EventEntity
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
import com.google.firebase.Timestamp
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
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventAttendanceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        HomeScreenViewModel.defaultIoDispatcher = testDispatcher
        HomeScreenViewModel.defaultTodayProvider = { LocalDate(2026, 9, 12) }
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
    fun event_getStatusForUser_returnsExpectedStatus() {
        val event = Event(
            uid = "event-1",
            creatorId = "user-creator",
            groupId = "group-1",
            title = "Test Match",
            fromDateTime = LocalDateTime(2026, 9, 12, 18, 0),
            toDateTime = LocalDateTime(2026, 9, 12, 20, 0),
            responses = mapOf(
                "user-accepted" to "ACCEPTED",
                "user-rejected" to "REJECTED",
                "user-invalid" to "UNKNOWN",
            )
        )

        assertEquals(EventResponseStatus.ACCEPTED, event.getStatusForUser("user-creator"))
        assertEquals(EventResponseStatus.ACCEPTED, event.getStatusForUser("user-accepted"))
        assertEquals(EventResponseStatus.REJECTED, event.getStatusForUser("user-rejected"))
        assertEquals(EventResponseStatus.NOT_SET, event.getStatusForUser("user-not-set"))
        assertEquals(EventResponseStatus.NOT_SET, event.getStatusForUser("user-invalid"))
    }

    @Test
    fun eventEntity_mapsToAndFromDomain() {
        val domainEvent = Event(
            uid = "event-entity-1",
            creatorId = "creator-1",
            groupId = "group-1",
            title = "Valorant scrims",
            eventIconUrl = "https://example.com/icon.png",
            fromDateTime = LocalDateTime(2026, 9, 12, 18, 0),
            toDateTime = LocalDateTime(2026, 9, 12, 20, 0),
            responses = mapOf("u1" to "ACCEPTED", "u2" to "REJECTED")
        )

        val entity = EventEntity.fromDomain(domainEvent)
        assertEquals(domainEvent.uid, entity.id)
        assertEquals(domainEvent.creatorId, entity.creatorId)
        assertEquals(domainEvent.groupId, entity.groupId)
        assertEquals(domainEvent.title, entity.title)
        assertEquals(domainEvent.responses, entity.responses)

        val mappedBack = entity.toDomain()
        assertEquals(domainEvent.uid, mappedBack.uid)
        assertEquals(domainEvent.responses, mappedBack.responses)
        assertEquals(EventResponseStatus.ACCEPTED, mappedBack.getStatusForUser("u1"))
        assertEquals(EventResponseStatus.REJECTED, mappedBack.getStatusForUser("u2"))
    }

    @Test
    fun homeScreenViewModel_mapsUserStatusAndPropagatesOnEventTap() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val currentUserId = "current-user"
        val currentUser = User(
            uid = currentUserId,
            username = "TestUser",
            email = "user@test.com",
            photoUrl = null,
            groups = listOf(Group(uid = "group-1", title = "Alpha Squad", members = listOf(currentUserId))),
        )

        val events = listOf(
            Event(
                uid = "event-other-accepted",
                creatorId = "other-user",
                groupId = "group-1",
                title = "Apex Games",
                fromDateTime = LocalDateTime(2026, 9, 12, 18, 0),
                toDateTime = LocalDateTime(2026, 9, 12, 20, 0),
                responses = mapOf(currentUserId to "ACCEPTED"),
            ),
            Event(
                uid = "event-own",
                creatorId = currentUserId,
                groupId = "group-1",
                title = "My Event",
                fromDateTime = LocalDateTime(2026, 9, 12, 18, 0),
                toDateTime = LocalDateTime(2026, 9, 12, 20, 0),
                responses = emptyMap(),
            )
        )

        val selectedDate = Date(
            dayOfMonth = 12,
            monthNumber = 9,
            year = 2026,
            countEvents = 2,
            isSelected = true,
            enabled = true,
        )

        val fakeCalendarProvider = FakeCalendarUIProvider(initialDates = listOf(selectedDate))
        val fakeEventProvider = FakeEventProvider(events = events)
        val fakeProfileProvider = FakeProfileProvider(user = currentUser)

        val viewModel = HomeScreenViewModel(
            navigator = fakeNavigator,
            authProvider = FakeAuthProvider(),
            calendarUIProvider = fakeCalendarProvider,
            eventProvider = fakeEventProvider,
            snackbar = FakeSnackbarProvider(),
            profileProvider = fakeProfileProvider,
            stringProvider = FakeStringProvider(),
            deepLinkManager = DefaultDeepLinkManager(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value as UiState.Normal
        val eventUIs = uiState.data.gameEventsOnDate
        assertEquals(2, eventUIs.size)

        val otherEventUI = eventUIs.first { it.eventId == "event-other-accepted" }
        assertFalse(otherEventUI.isYourEvent)
        assertEquals(EventResponseStatus.ACCEPTED, otherEventUI.userStatus)

        val ownEventUI = eventUIs.first { it.eventId == "event-own" }
        assertTrue(ownEventUI.isYourEvent)
        assertEquals(EventResponseStatus.ACCEPTED, ownEventUI.userStatus)

        viewModel.onAction(HomeScreenAction.OnEventTap(otherEventUI))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigatedDestinations.size)
        val dest = fakeNavigator.navigatedDestinations.first() as Destination.EventDetailsScreen
        assertEquals("event-other-accepted", dest.eventId)
        assertEquals(EventResponseStatus.ACCEPTED, dest.userStatus)
        assertFalse(dest.isYourEvent)
        assertEquals("group-1", dest.groupId)
    }

    @Test
    fun eventDetailsScreenViewModel_acceptAndReject_toggleLogic() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val fakeEventProvider = FakeEventProvider()
        val currentUser = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(user = currentUser)

        val viewModel = EventDetailsScreenViewModel(
            navigator = fakeNavigator,
            eventProvider = fakeEventProvider,
            profileProvider = fakeProfileProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.initData(
            Destination.EventDetailsScreen(
                eventId = "event-100",
                title = "CS:GO Finals",
                date = "18:00 - 20:00",
                imageUrl = null,
                isYourEvent = false,
                userStatus = EventResponseStatus.NOT_SET,
            )
        )

        assertEquals(EventResponseStatus.NOT_SET, viewModel.uiState.value.userStatus)

        // 1. Tap Accept -> status becomes ACCEPTED
        viewModel.onAction(EventDetailsScreenAction.OnAcceptTap)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.ACCEPTED, viewModel.uiState.value.userStatus)
        assertEquals("event-100", fakeEventProvider.lastUpdatedResponseEventId)
        assertEquals("user-1", fakeEventProvider.lastUpdatedResponseUserId)
        assertEquals(EventResponseStatus.ACCEPTED, fakeEventProvider.lastUpdatedResponseStatus)

        // 2. Tap Accept again -> toggles back to NOT_SET
        viewModel.onAction(EventDetailsScreenAction.OnAcceptTap)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.NOT_SET, viewModel.uiState.value.userStatus)
        assertEquals(EventResponseStatus.NOT_SET, fakeEventProvider.lastUpdatedResponseStatus)

        // 3. Tap Reject -> status becomes REJECTED
        viewModel.onAction(EventDetailsScreenAction.OnRejectTap)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.REJECTED, viewModel.uiState.value.userStatus)
        assertEquals(EventResponseStatus.REJECTED, fakeEventProvider.lastUpdatedResponseStatus)

        // 4. Tap Reject again -> toggles back to NOT_SET
        viewModel.onAction(EventDetailsScreenAction.OnRejectTap)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.NOT_SET, viewModel.uiState.value.userStatus)
        assertEquals(EventResponseStatus.NOT_SET, fakeEventProvider.lastUpdatedResponseStatus)

        // 5. Direct updateEventResponse(eventId, userId, status)
        viewModel.updateEventResponse("event-100", "user-1", EventResponseStatus.ACCEPTED)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.ACCEPTED, viewModel.uiState.value.userStatus)
        assertEquals(EventResponseStatus.ACCEPTED, fakeEventProvider.lastUpdatedResponseStatus)
    }

    @Test
    fun eventDetailsScreenViewModel_creatorCannotToggleResponse() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val fakeEventProvider = FakeEventProvider()
        val currentUser = User(
            uid = "user-creator",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(user = currentUser)

        val viewModel = EventDetailsScreenViewModel(
            navigator = fakeNavigator,
            eventProvider = fakeEventProvider,
            profileProvider = fakeProfileProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.initData(
            Destination.EventDetailsScreen(
                eventId = "event-creator-1",
                title = "CS:GO Finals",
                date = "18:00 - 20:00",
                imageUrl = null,
                isYourEvent = true,
                userStatus = EventResponseStatus.ACCEPTED,
            )
        )

        assertEquals(EventResponseStatus.ACCEPTED, viewModel.uiState.value.userStatus)
        assertTrue(viewModel.uiState.value.isYourEvent)

        // Attempting to accept/reject does not alter response or call eventProvider
        viewModel.onAction(EventDetailsScreenAction.OnRejectTap)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EventResponseStatus.ACCEPTED, viewModel.uiState.value.userStatus)
        assertEquals(null, fakeEventProvider.lastUpdatedResponseEventId)
    }

    @Test
    fun eventDetailsScreenViewModel_loadsGroupMembersWithAttendanceResponses() = runTest(testDispatcher) {
        val fakeNavigator = FakeNavigator()
        val event = Event(
            uid = "event-1",
            creatorId = "user-creator",
            groupId = "group-1",
            title = "Apex Games",
            fromDateTime = LocalDateTime(2026, 9, 12, 18, 0),
            toDateTime = LocalDateTime(2026, 9, 12, 20, 0),
            responses = mapOf(
                "user-accepted" to "ACCEPTED",
                "user-rejected" to "REJECTED",
            ),
        )
        val fakeEventProvider = FakeEventProvider(events = listOf(event))
        val group = Group(
            uid = "group-1",
            title = "Alpha Squad",
            members = listOf("user-creator", "user-accepted", "user-rejected", "user-not-set"),
        )
        val members = listOf(
            Friend(uid = "user-creator", username = "CreatorUser", groupTitleFrom = "Alpha Squad", photoUrl = null),
            Friend(uid = "user-accepted", username = "AcceptedUser", groupTitleFrom = "Alpha Squad", photoUrl = "https://photo.url"),
            Friend(uid = "user-rejected", username = "RejectedUser", groupTitleFrom = "Alpha Squad", photoUrl = null),
            Friend(uid = "user-not-set", username = "NotSetUser", groupTitleFrom = "Alpha Squad", photoUrl = null),
        )
        val fakeProfileProvider = FakeProfileProvider(
            user = User(uid = "user-accepted", username = "AcceptedUser", email = "accepted@test.com", photoUrl = null, groups = listOf(group)),
            groupInfo = group,
            groupSections = listOf(UserGroupSection(groupId = "group-1", title = "Alpha Squad", members = members)),
        )

        val viewModel = EventDetailsScreenViewModel(
            navigator = fakeNavigator,
            eventProvider = fakeEventProvider,
            profileProvider = fakeProfileProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.initData(
            Destination.EventDetailsScreen(
                eventId = "event-1",
                title = "Apex Games",
                date = "18:00 - 20:00",
                imageUrl = null,
                isYourEvent = false,
                userStatus = EventResponseStatus.ACCEPTED,
                groupId = "group-1",
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("group-1", uiState.groupId)
        assertEquals(4, uiState.members.size)

        val creatorMember = uiState.members.first { it.uid == "user-creator" }
        assertEquals(EventResponseStatus.ACCEPTED, creatorMember.status)
        assertEquals("CreatorUser", creatorMember.username)

        val acceptedMember = uiState.members.first { it.uid == "user-accepted" }
        assertEquals(EventResponseStatus.ACCEPTED, acceptedMember.status)

        val rejectedMember = uiState.members.first { it.uid == "user-rejected" }
        assertEquals(EventResponseStatus.REJECTED, rejectedMember.status)

        val notSetMember = uiState.members.first { it.uid == "user-not-set" }
        assertEquals(EventResponseStatus.NOT_SET, notSetMember.status)

        // Tapping reject as user-accepted updates optimistic status in members list
        viewModel.onAction(EventDetailsScreenAction.OnRejectTap)
        assertEquals(EventResponseStatus.REJECTED, viewModel.uiState.value.userStatus)
        val updatedAcceptedMember = viewModel.uiState.value.members.first { it.uid == "user-accepted" }
        assertEquals(EventResponseStatus.REJECTED, updatedAcceptedMember.status)
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
        var groupInfo: Group? = null,
        var groupSections: List<UserGroupSection> = emptyList(),
    ) : ProfileProvider {
        override fun getUserInfoFlow(): Flow<User?> = flowOf(user)
        override fun createNewInviteLink(inviteGroupId: String): String = ""
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = groupInfo
        override suspend fun createNewUserGroup(userId: String, title: String): String = ""
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(groupSections)
    }

    private class FakeCalendarUIProvider(
        var initialDates: List<Date> = emptyList(),
    ) : CalendarUIProvider {
        override fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI {
            return CalendarUI(daysOfWeek = emptyList(), yearMonth = yearMonth, dates = initialDates)
        }
        override fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI = target
        override fun mergedCalendarWithEvents(
            calendar: CalendarUI,
            events: List<Event>,
            currentUserId: String
        ): CalendarUI = calendar
    }

    private class FakeEventProvider(
        var events: List<Event> = emptyList()
    ) : EventProvider {
        var lastUpdatedResponseEventId: String? = null
        var lastUpdatedResponseUserId: String? = null
        var lastUpdatedResponseStatus: EventResponseStatus? = null

        override suspend fun saveEventData(event: Event): Boolean = true
        override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = flowOf(events)
        override suspend fun deleteEvent(eventId: String): Boolean = true
        override suspend fun updateEventResponse(
            eventId: String,
            userId: String,
            status: EventResponseStatus,
        ) {
            lastUpdatedResponseEventId = eventId
            lastUpdatedResponseUserId = userId
            lastUpdatedResponseStatus = status
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
