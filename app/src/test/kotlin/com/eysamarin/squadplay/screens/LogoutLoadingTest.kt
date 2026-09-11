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
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.DeepLinkManager
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.main.HomeScreenViewModel
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogoutLoadingTest {

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
    fun homeScreenViewModel_onLogOutTap_setsLoadingIndicatorUntilComplete() = runTest(testDispatcher) {
        val fakeAuth = FakeAuthProvider()
        val fakeNavigator = FakeNavigator()
        val viewModel = createHomeScreenViewModel(fakeAuth, fakeNavigator)

        assertFalse(viewModel.isLoggingOut.value)

        val signOutGate = CompletableDeferred<Boolean>()
        fakeAuth.signOutDeferred = signOutGate

        viewModel.onLogOutTap()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isLoggingOut.value)
        assertEquals(0, fakeNavigator.navigateToAuthGraphCalls)

        signOutGate.complete(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigateToAuthGraphCalls)
    }

    @Test
    fun homeScreenViewModel_onLogOutTap_resetsLoadingWhenFailed() = runTest(testDispatcher) {
        val fakeAuth = FakeAuthProvider()
        val fakeNavigator = FakeNavigator()
        val viewModel = createHomeScreenViewModel(fakeAuth, fakeNavigator)

        val signOutGate = CompletableDeferred<Boolean>()
        fakeAuth.signOutDeferred = signOutGate

        viewModel.onLogOutTap()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isLoggingOut.value)

        signOutGate.complete(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoggingOut.value)
        assertEquals(0, fakeNavigator.navigateToAuthGraphCalls)
    }

    @Test
    fun profileScreenViewModel_onLogOutTap_setsLoadingIndicatorUntilComplete() = runTest(testDispatcher) {
        val fakeAuth = FakeAuthProvider()
        val fakeNavigator = FakeNavigator()
        val viewModel = ProfileScreenViewModel(
            navigator = fakeNavigator,
            profileProvider = FakeProfileProvider(),
            authProvider = fakeAuth,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )

        assertFalse(viewModel.isLoggingOut.value)

        val signOutGate = CompletableDeferred<Boolean>()
        fakeAuth.signOutDeferred = signOutGate

        viewModel.onLogOutTap()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isLoggingOut.value)
        assertEquals(0, fakeNavigator.navigateToAuthGraphCalls)

        signOutGate.complete(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeNavigator.navigateToAuthGraphCalls)
    }

    private fun createHomeScreenViewModel(
        authProvider: AuthProvider,
        navigator: Navigator,
    ): HomeScreenViewModel {
        return HomeScreenViewModel(
            navigator = navigator,
            authProvider = authProvider,
            calendarUIProvider = FakeCalendarUIProvider(),
            eventProvider = FakeEventProvider(),
            snackbar = FakeSnackbarProvider(),
            profileProvider = FakeProfileProvider(),
            stringProvider = FakeStringProvider(),
            deepLinkManager = com.eysamarin.squadplay.navigation.DefaultDeepLinkManager(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
    }

    private class FakeAuthProvider : AuthProvider {
        var signOutDeferred: CompletableDeferred<Boolean>? = null

        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signOut(): Boolean {
            return signOutDeferred?.await() ?: true
        }
        override suspend fun isUserExists(): Boolean = true
    }

    private class FakeNavigator : Navigator {
        var navigateToAuthGraphCalls = 0
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination, navOptions: NavOptionsBuilder.() -> Unit) {}
        override suspend fun navigateToHomeGraph() {}
        override suspend fun navigateToAuthGraph() {
            navigateToAuthGraphCalls++
        }
        override suspend fun navigateUp() {}
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

    private class FakeCalendarUIProvider : CalendarUIProvider {
        override fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI {
            return CalendarUI(daysOfWeek = emptyList(), yearMonth = yearMonth, dates = emptyList())
        }
        override fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI = target
        override fun mergedCalendarWithEvents(
            calendar: CalendarUI,
            events: List<Event>,
            currentUserId: String
        ): CalendarUI = calendar
    }

    private class FakeEventProvider : EventProvider {
        override suspend fun saveEventData(event: Event): Boolean = true
        override fun getEventsFlow(groupId: String): Flow<List<Event>> = flowOf(emptyList())
        override suspend fun deleteEvent(eventId: String): Boolean = true
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
