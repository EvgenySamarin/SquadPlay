package com.eysamarin.squadplay.navigation

import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeGraphViewModelTest {

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
    fun pendingDeepLink_showsConfirmationDialog_whenUserNotInSquad() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val group = Group(
            uid = "group-100",
            title = "Apex Champions",
            members = listOf("user-2"),
        )
        val deepLinkManager = DefaultDeepLinkManager().apply {
            setPendingInviteGroupId("group-100")
        }
        val fakeProfileProvider = FakeProfileProvider(userInfo = user, groups = mapOf("group-100" to group))
        val fakeSnackbar = FakeSnackbarProvider()
        val fakeStringProvider = FakeStringProvider()
        val fakeAnalyticsProvider = FakeAnalyticsProvider()

        val viewModel = HomeGraphViewModel(
            deepLinkManager = deepLinkManager,
            profileProvider = fakeProfileProvider,
            snackbar = fakeSnackbar,
            stringProvider = fakeStringProvider,
            analyticsProvider = fakeAnalyticsProvider,
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.confirmInviteDialogState.value
        assertTrue(state is UiState.Normal)
        assertEquals("Want to join Apex Champions?", (state as UiState.Normal).data)
        assertNull(deepLinkManager.pendingInviteGroupId.value)
    }

    @Test
    fun pendingDeepLink_showsAlreadyInSquadSnackbar_whenUserAlreadyMember() = runTest(testDispatcher) {
        val existingGroup = Group(uid = "group-100", title = "Apex Champions", members = listOf("user-1"))
        val user = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = listOf(existingGroup),
        )
        val deepLinkManager = DefaultDeepLinkManager().apply {
            setPendingInviteGroupId("group-100")
        }
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val fakeSnackbar = FakeSnackbarProvider()
        val fakeStringProvider = FakeStringProvider()

        val viewModel = HomeGraphViewModel(
            deepLinkManager = deepLinkManager,
            profileProvider = fakeProfileProvider,
            snackbar = fakeSnackbar,
            stringProvider = fakeStringProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.confirmInviteDialogState.value)
        assertEquals(fakeStringProvider.alreadyInSquad, fakeSnackbar.lastMessage)
        assertNull(deepLinkManager.pendingInviteGroupId.value)
    }

    @Test
    fun pendingDeepLink_showsSquadNotFoundSnackbar_whenGroupDoesNotExist() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val deepLinkManager = DefaultDeepLinkManager().apply {
            setPendingInviteGroupId("non-existent-group")
        }
        val fakeProfileProvider = FakeProfileProvider(userInfo = user, groups = emptyMap())
        val fakeSnackbar = FakeSnackbarProvider()
        val fakeStringProvider = FakeStringProvider()

        val viewModel = HomeGraphViewModel(
            deepLinkManager = deepLinkManager,
            profileProvider = fakeProfileProvider,
            snackbar = fakeSnackbar,
            stringProvider = fakeStringProvider,
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.confirmInviteDialogState.value)
        assertEquals(fakeStringProvider.squadNotFound("non-existent-group"), fakeSnackbar.lastMessage)
        assertNull(deepLinkManager.pendingInviteGroupId.value)
    }

    @Test
    fun onJoinGroupDialogConfirm_joinsSquad_andClearsDialogState() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val group = Group(
            uid = "group-100",
            title = "Apex Champions",
            members = listOf("user-2"),
        )
        val deepLinkManager = DefaultDeepLinkManager().apply {
            setPendingInviteGroupId("group-100")
        }
        val fakeProfileProvider = FakeProfileProvider(userInfo = user, groups = mapOf("group-100" to group))
        val fakeSnackbar = FakeSnackbarProvider()
        val fakeStringProvider = FakeStringProvider()
        val fakeAnalyticsProvider = FakeAnalyticsProvider()

        val viewModel = HomeGraphViewModel(
            deepLinkManager = deepLinkManager,
            profileProvider = fakeProfileProvider,
            snackbar = fakeSnackbar,
            stringProvider = fakeStringProvider,
            analyticsProvider = fakeAnalyticsProvider,
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onJoinGroupDialogConfirm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.confirmInviteDialogState.value)
        assertEquals(1, fakeProfileProvider.joinGroupCalls)
        assertEquals("user-1", fakeProfileProvider.lastJoinUserId)
        assertEquals("group-100", fakeProfileProvider.lastJoinGroupId)
        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.JoinGroup("group-100")))
        assertEquals(fakeStringProvider.joinedSquad, fakeSnackbar.lastMessage)
    }

    @Test
    fun onJoinGroupDialogDismiss_clearsDialogState() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Gamer",
            email = "gamer@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val group = Group(
            uid = "group-100",
            title = "Apex Champions",
            members = listOf("user-2"),
        )
        val deepLinkManager = DefaultDeepLinkManager().apply {
            setPendingInviteGroupId("group-100")
        }
        val fakeProfileProvider = FakeProfileProvider(userInfo = user, groups = mapOf("group-100" to group))

        val viewModel = HomeGraphViewModel(
            deepLinkManager = deepLinkManager,
            profileProvider = fakeProfileProvider,
            snackbar = FakeSnackbarProvider(),
            stringProvider = FakeStringProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onJoinGroupDialogDismiss()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.confirmInviteDialogState.value)
    }

    private class FakeProfileProvider(
        private val userInfo: User? = null,
        private val groups: Map<String, Group> = emptyMap(),
    ) : ProfileProvider {
        var joinGroupCalls = 0
        var lastJoinUserId: String? = null
        var lastJoinGroupId: String? = null

        override fun getUserInfoFlow(): Flow<User?> = flowOf(userInfo)
        override suspend fun getGroupInfo(groupId: String): Group? = groups[groupId]

        override suspend fun joinGroup(userId: String, groupId: String): Boolean {
            joinGroupCalls++
            lastJoinUserId = userId
            lastJoinGroupId = groupId
            return true
        }

        override fun createNewInviteLink(inviteGroupId: String): String = "https://invite/$inviteGroupId"
        override suspend fun createNewUserGroup(userId: String, title: String): String = "new-group"
        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(emptyList())
        override suspend fun renameGroup(groupId: String, newTitle: String): Boolean = true
        override suspend fun deleteGroup(groupId: String): Boolean = true
        override suspend fun leaveGroup(userId: String, groupId: String): Boolean = true
        override suspend fun updateNickname(userId: String, nickname: String): Boolean = true
    }

    private class FakeSnackbarProvider : SnackbarProvider {
        var lastMessage: String? = null
        override val messagesChannel: Flow<String> = flowOf()
        override suspend fun showMessage(message: String) {
            lastMessage = message
        }
    }

    private class FakeStringProvider : StringProvider {
        override val cannotSignText: String = ""
        override val alreadyInSquad: String = "Already in squad"
        override val joinedSquad: String = "Joined squad"
        override val joinSquadFailed: String = "Failed to join squad"
        override fun squadNotFound(groupId: String): String = "Squad not found: $groupId"
        override fun wantToJoinSquad(groupTitle: String): String = "Want to join $groupTitle?"
        override fun fromToDate(fromDate: String, toDate: String): String = ""
        override val youHaveNoSquad: String = ""
        override val eventSaved: String = ""
        override val eventSaveFailed: String = ""
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
