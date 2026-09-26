package com.eysamarin.squadplay.screens

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
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.main.HomeScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenLoadingTimeoutTest {

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
    fun `timeout triggers isTimeoutDialogVisible when data takes longer than timeout`() = runTest(testDispatcher) {
        val userFlow = MutableSharedFlow<User?>()
        val viewModel = createViewModel(userFlow)
        viewModel.loadingTimeoutMillis = 1000L

        // Trigger data collection with the new timeout
        viewModel.onRetryLoadingTap()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is UiState.Loading)
        assertFalse(viewModel.isTimeoutDialogVisible.value)

        advanceTimeBy(500)
        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.isTimeoutDialogVisible.value)

        advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.isTimeoutDialogVisible.value)
    }

    @Test
    fun `successful data load before timeout cancels timeout and keeps dialog hidden`() = runTest(testDispatcher) {
        val userFlow = MutableSharedFlow<User?>()
        val viewModel = createViewModel(userFlow)
        viewModel.loadingTimeoutMillis = 1000L

        viewModel.onRetryLoadingTap()
        testDispatcher.scheduler.runCurrent()

        advanceTimeBy(300)
        userFlow.emit(User(uid = "user1", username = "tester", email = "test@example.com", photoUrl = null, groups = emptyList()))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is UiState.Normal)
        assertFalse(viewModel.isTimeoutDialogVisible.value)

        // Advance beyond the original timeout; dialog must remain hidden
        advanceTimeBy(1500)
        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.isTimeoutDialogVisible.value)
        assertTrue(viewModel.uiState.value is UiState.Normal)
    }

    @Test
    fun `onRetryLoadingTap resets timeout and restarts data loading`() = runTest(testDispatcher) {
        val userFlow = MutableSharedFlow<User?>()
        val viewModel = createViewModel(userFlow)
        viewModel.loadingTimeoutMillis = 1000L

        viewModel.onRetryLoadingTap()
        advanceTimeBy(1100)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.isTimeoutDialogVisible.value)

        // User taps Retry action
        viewModel.onAction(HomeScreenAction.OnRetryLoadingTap)
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.isTimeoutDialogVisible.value)
        assertTrue(viewModel.uiState.value is UiState.Loading)

        // Now data arrives before the new timeout expires
        advanceTimeBy(500)
        userFlow.emit(User(uid = "user1", username = "tester", email = "test@example.com", photoUrl = null, groups = emptyList()))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is UiState.Normal)
        assertFalse(viewModel.isTimeoutDialogVisible.value)
    }

    @Test
    fun `onDismissTimeoutDialog hides dialog while keeping current ui state`() = runTest(testDispatcher) {
        val userFlow = MutableSharedFlow<User?>()
        val viewModel = createViewModel(userFlow)
        viewModel.loadingTimeoutMillis = 1000L

        viewModel.onRetryLoadingTap()
        advanceTimeBy(1100)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.isTimeoutDialogVisible.value)

        // User dismisses dialog
        viewModel.onAction(HomeScreenAction.OnDismissTimeoutDialog)
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.isTimeoutDialogVisible.value)
    }

    @Test
    fun `flow error triggers isTimeoutDialogVisible immediately`() = runTest(testDispatcher) {
        val errorFlow = flow<User?> {
            throw RuntimeException("Network connection failed")
        }
        val viewModel = createViewModel(errorFlow)
        viewModel.loadingTimeoutMillis = 5000L

        viewModel.onRetryLoadingTap()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isTimeoutDialogVisible.value)
    }

    @Test
    fun `initData resets isTimeoutDialogVisible and isLoggingOut and restarts data collection`() = runTest(testDispatcher) {
        val userFlow = MutableSharedFlow<User?>()
        val viewModel = createViewModel(userFlow)
        viewModel.loadingTimeoutMillis = 1000L

        viewModel.initData()
        advanceTimeBy(1100)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.isTimeoutDialogVisible.value)

        viewModel.initData()
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.isTimeoutDialogVisible.value)
        assertFalse(viewModel.isLoggingOut.value)
        assertTrue(viewModel.uiState.value is UiState.Loading)

        userFlow.emit(User(uid = "user1", username = "tester", email = "test@example.com", photoUrl = null, groups = emptyList()))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is UiState.Normal)
    }

    private fun createViewModel(userFlow: Flow<User?>): HomeScreenViewModel {
        val fakeProfile = object : FakeProfileProvider() {
            override fun getUserInfoFlow(): Flow<User?> = userFlow
        }
        val vm = HomeScreenViewModel(
            navigator = FakeNavigator(),
            snackbar = FakeSnackbarProvider(),
            calendarUIProvider = FakeCalendarUIProvider(),
            eventProvider = FakeEventProvider(),
            authProvider = FakeAuthProvider(),
            profileProvider = fakeProfile,
            stringProvider = FakeStringProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
        vm.ioDispatcher = testDispatcher
        return vm
    }

    private open class FakeProfileProvider : ProfileProvider {
        override fun getUserInfoFlow(): Flow<User?> = flowOf(null)
        override fun createNewInviteLink(inviteGroupId: String): String = ""
        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null
        override suspend fun createNewUserGroup(userId: String, title: String): String = ""
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(emptyList())
        override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
        override suspend fun deleteGroup(groupId: String): Boolean = true
        override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
        override suspend fun updateNickname(userId: String, nickname: String): Boolean = true
    }

    private class FakeAuthProvider : AuthProvider {
        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signOut(): Boolean = true
        override suspend fun isUserExists(): Boolean = true
    }

    private class FakeNavigator : Navigator {
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination) {}
        override suspend fun navigateToHomeGraph() {}
        override suspend fun navigateToAuthGraph() {}
        override suspend fun navigateUp() {}
    }

    private class FakeCalendarUIProvider : CalendarUIProvider {
        override fun provideCalendarUIBy(yearMonth: LocalDate): CalendarUI {
            return CalendarUI(daysOfWeek = emptyList(), yearMonth = yearMonth, dates = emptyList())
        }
        override fun updateCalendarBySelectedDate(target: CalendarUI, selectedDate: Date): CalendarUI = target
        override fun mergedCalendarWithEvents(
            calendar: CalendarUI,
            events: List<Event>,
            currentUserId: String,
            groupMembers: Map<String, List<Friend>>
        ): CalendarUI = calendar
    }

    private class FakeEventProvider : EventProvider {
        override suspend fun saveEventData(event: Event): Boolean = true
        override fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>> = flowOf(emptyList())
        override suspend fun deleteEvent(eventId: String): Boolean = true
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
        override fun fromToDate(fromDate: String, toDate: String): String = ""
        override val youHaveNoSquad: String = ""
        override val eventSaved: String = ""
        override val eventSaveFailed: String = ""
        override val joinedSquad: String = ""
        override val joinSquadFailed: String = ""
    }

    private class FakeAnalyticsProvider : AnalyticsProvider {
        override fun trackEvent(event: AnalyticsEvent) {}
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
