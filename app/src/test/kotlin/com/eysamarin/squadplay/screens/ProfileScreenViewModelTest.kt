package com.eysamarin.squadplay.screens

import androidx.navigation.NavOptionsBuilder
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.NavigationAction
import com.eysamarin.squadplay.navigation.Navigator
import com.eysamarin.squadplay.screens.profile.ProfileScreenViewModel
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileScreenViewModelTest {

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
    fun collectUserInfo_withGroups_emitsGroupSectionsInUiState() = runTest(testDispatcher) {
        val group1 = Group(uid = "group-1", title = "Warriors", members = listOf("user-2"))
        val group2 = Group(uid = "group-2", title = "Mages", members = listOf("user-3"))
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = listOf(group1, group2),
        )
        val expectedSections = listOf(
            UserGroupSection(
                groupId = "group-1",
                title = "Warriors",
                members = listOf(
                    Friend(uid = "user-2", username = "Warrior1", groupTitleFrom = "Warriors", photoUrl = null)
                )
            ),
            UserGroupSection(
                groupId = "group-2",
                title = "Mages",
                members = listOf(
                    Friend(uid = "user-3", username = "Mage1", groupTitleFrom = "Mages", photoUrl = null)
                )
            )
        )

        val fakeProfileProvider = FakeProfileProvider(
            userInfo = user,
            sections = expectedSections,
        )
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertEquals("Leader", data.user.username)
        assertEquals(2, data.groupSections.size)
        assertEquals("Warriors", data.groupSections[0].title)
        assertEquals(1, data.groupSections[0].members.size)
        assertEquals("Mages", data.groupSections[1].title)
    }

    @Test
    fun collectUserInfo_withoutGroups_emitsEmptyGroupSections() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Solo Player",
            email = "solo@test.com",
            photoUrl = null,
            groups = emptyList(),
        )

        val fakeProfileProvider = FakeProfileProvider(
            userInfo = user,
            sections = emptyList(),
        )
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Normal)
        val data = (state as UiState.Normal).data
        assertEquals(0, data.groupSections.size)
    }

    @Test
    fun onCreateInviteLinkTap_generatesInviteLinkForTargetGroupWithoutCreatingNewGroup() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = emptyList(),
        )

        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnCreateInviteLinkTap("target-group-123"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeProfileProvider.createNewUserGroupCalls)
        assertEquals("target-group-123", fakeProfileProvider.lastInviteGroupId)

        val inviteState = viewModel.inviteLinkState.value
        assertTrue(inviteState is UiState.Normal)
        assertEquals("https://evgenysamarin.github.io/invite/target-group-123", (inviteState as UiState.Normal).data)
    }

    @Test
    fun hideShareLink_resetsInviteLinkStateToEmpty() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnCreateInviteLinkTap("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.inviteLinkState.value is UiState.Normal)

        viewModel.hideShareLink()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.inviteLinkState.value is UiState.Empty)
    }

    private fun createViewModel(
        profileProvider: ProfileProvider = FakeProfileProvider(),
        navigator: Navigator = FakeNavigator(),
    ): ProfileScreenViewModel {
        return ProfileScreenViewModel(
            navigator = navigator,
            profileProvider = profileProvider,
            authProvider = FakeAuthProvider(),
            analyticsProvider = FakeAnalyticsProvider(),
            logger = FakeAppLogger(),
        )
    }

    private class FakeProfileProvider(
        var userInfo: User? = null,
        var sections: List<UserGroupSection> = emptyList(),
    ) : ProfileProvider {
        var createNewUserGroupCalls = 0
        var lastInviteGroupId: String? = null

        override fun getUserInfoFlow(): Flow<User?> = flowOf(userInfo)

        override fun createNewInviteLink(inviteGroupId: String): String {
            lastInviteGroupId = inviteGroupId
            return "https://evgenysamarin.github.io/invite/$inviteGroupId"
        }

        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null

        override suspend fun createNewUserGroup(userId: String): String {
            createNewUserGroupCalls++
            return "new-group-id"
        }

        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(sections)
    }

    private class FakeNavigator : Navigator {
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination, navOptions: NavOptionsBuilder.() -> Unit) {}
        override suspend fun navigateToHomeGraph() {}
        override suspend fun navigateToAuthGraph() {}
        override suspend fun navigateUp() {}
    }

    private class FakeAuthProvider : AuthProvider {
        override suspend fun signInWithGoogle(): Boolean = true
        override suspend fun signUpWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signInWithEmailPassword(email: String, password: String): UiState<Boolean> = UiState.Normal(true)
        override suspend fun signOut(): Boolean = true
        override suspend fun isUserExists(): Boolean = true
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
