package com.eysamarin.squadplay.screens

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
import org.junit.Assert.assertFalse
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
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(
            profileProvider = fakeProfileProvider,
            analyticsProvider = fakeAnalyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnCreateInviteLinkTap("target-group-123"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeProfileProvider.createNewUserGroupCalls)
        assertEquals("target-group-123", fakeProfileProvider.lastInviteGroupId)
        assertEquals(listOf(AnalyticsEvent.ShareInviteClicked("target-group-123")), fakeAnalyticsProvider.trackedEvents)

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

    @Test
    fun onCreateNewGroupTap_setsBottomSheetVisibleToTrue() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val viewModel = createViewModel(profileProvider = FakeProfileProvider(userInfo = user))
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value as UiState.Normal
        assertEquals(false, state.data.isCreateGroupBottomSheetVisible)

        viewModel.onAction(ProfileScreenAction.OnCreateNewGroupTap)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value as UiState.Normal
        assertEquals(true, state.data.isCreateGroupBottomSheetVisible)
    }

    @Test
    fun onDismissCreateGroupBottomSheet_setsBottomSheetVisibleToFalseWithoutCreatingGroup() = runTest(testDispatcher) {
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

        viewModel.onAction(ProfileScreenAction.OnCreateNewGroupTap)
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value as UiState.Normal
        assertEquals(true, state.data.isCreateGroupBottomSheetVisible)

        viewModel.onAction(ProfileScreenAction.OnDismissCreateGroupBottomSheet)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value as UiState.Normal
        assertEquals(false, state.data.isCreateGroupBottomSheetVisible)
        assertEquals(0, fakeProfileProvider.createNewUserGroupCalls)
    }

    @Test
    fun onConfirmCreateGroup_dismissesBottomSheet_createsGroupWithTitle_andTracksAnalyticsEvent() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Leader",
            email = "leader@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(
            profileProvider = fakeProfileProvider,
            analyticsProvider = fakeAnalyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnCreateNewGroupTap)
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value as UiState.Normal
        assertEquals(true, state.data.isCreateGroupBottomSheetVisible)

        viewModel.onAction(ProfileScreenAction.OnConfirmCreateGroup("Squad"))
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value as UiState.Normal
        assertEquals(false, state.data.isCreateGroupBottomSheetVisible)
        assertEquals(1, fakeProfileProvider.createNewUserGroupCalls)
        assertEquals("user-1", fakeProfileProvider.lastCreatedGroupUserId)
        assertEquals("Squad", fakeProfileProvider.lastCreatedGroupTitle)
        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.GroupCreated("new-group-id")))
    }

    @Test
    fun onEditGroupTap_tracksEditGroupClickedAnalyticsEvent() = runTest(testDispatcher) {
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(analyticsProvider = fakeAnalyticsProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnEditGroupTap("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.EditGroupClicked("group-1")))
    }

    @Test
    fun onConfirmEditGroup_renamesGroup_andTracksGroupRenamedAnalyticsEvent() = runTest(testDispatcher) {
        val fakeProfileProvider = FakeProfileProvider()
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(
            profileProvider = fakeProfileProvider,
            analyticsProvider = fakeAnalyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmEditGroup("group-1", "Titans"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeProfileProvider.renameGroupCalls)
        assertEquals("group-1", fakeProfileProvider.lastRenamedGroupId)
        assertEquals("Titans", fakeProfileProvider.lastRenamedGroupTitle)
        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.GroupRenamed("group-1")))
    }

    @Test
    fun onConfirmEditGroup_ignoresMultipleWords() = runTest(testDispatcher) {
        val fakeProfileProvider = FakeProfileProvider()
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmEditGroup("group-1", "Multiple Words Here"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeProfileProvider.renameGroupCalls)
    }

    @Test
    fun onDeleteGroupTap_tracksDeleteGroupClickedAnalyticsEvent() = runTest(testDispatcher) {
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(analyticsProvider = fakeAnalyticsProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnDeleteGroupTap("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.DeleteGroupClicked("group-1")))
    }

    @Test
    fun onConfirmDeleteGroup_deletesGroup_andTracksGroupDeletedAnalyticsEvent() = runTest(testDispatcher) {
        val fakeProfileProvider = FakeProfileProvider()
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(
            profileProvider = fakeProfileProvider,
            analyticsProvider = fakeAnalyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmDeleteGroup("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeProfileProvider.deleteGroupCalls)
        assertEquals("group-1", fakeProfileProvider.lastDeletedGroupId)
        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.GroupDeleted("group-1")))
    }

    @Test
    fun onLeaveGroupTap_tracksLeaveGroupClickedAnalyticsEvent() = runTest(testDispatcher) {
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(analyticsProvider = fakeAnalyticsProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnLeaveGroupTap("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.LeaveGroupClicked("group-1")))
    }

    @Test
    fun onConfirmLeaveGroup_leavesGroup_andTracksGroupLeftAnalyticsEvent() = runTest(testDispatcher) {
        val user = User(
            uid = "user-123",
            username = "Member",
            email = "member@test.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val fakeAnalyticsProvider = FakeAnalyticsProvider()
        val viewModel = createViewModel(
            profileProvider = fakeProfileProvider,
            analyticsProvider = fakeAnalyticsProvider,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmLeaveGroup("group-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeProfileProvider.leaveGroupCalls)
        assertEquals("user-123", fakeProfileProvider.lastLeaveGroupUserId)
        assertEquals("group-1", fakeProfileProvider.lastLeaveGroupId)
        assertTrue(fakeAnalyticsProvider.trackedEvents.contains(AnalyticsEvent.GroupLeft("group-1")))
    }

    @Test
    fun onAvatarTap_showsChangeNicknameBottomSheet() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Tester",
            email = "test@example.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val viewModel = createViewModel(profileProvider = FakeProfileProvider(userInfo = user))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnAvatarTap)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Normal)
        assertTrue((uiState as UiState.Normal).data.isChangeNicknameBottomSheetVisible)
    }

    @Test
    fun onDismissChangeNicknameBottomSheet_hidesBottomSheet() = runTest(testDispatcher) {
        val user = User(
            uid = "user-1",
            username = "Tester",
            email = "test@example.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val viewModel = createViewModel(profileProvider = FakeProfileProvider(userInfo = user))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnAvatarTap)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(ProfileScreenAction.OnDismissChangeNicknameBottomSheet)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Normal)
        assertFalse((uiState as UiState.Normal).data.isChangeNicknameBottomSheetVisible)
    }

    @Test
    fun onConfirmChangeNickname_updatesNickname_andHidesBottomSheet() = runTest(testDispatcher) {
        val user = User(
            uid = "user-123",
            username = "Tester",
            email = "test@example.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnAvatarTap)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(ProfileScreenAction.OnConfirmChangeNickname("shadow_ninja"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeProfileProvider.updateNicknameCalls)
        assertEquals("user-123", fakeProfileProvider.lastUpdateNicknameUserId)
        assertEquals("shadow_ninja", fakeProfileProvider.lastUpdateNickname)
        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Normal)
        assertFalse((uiState as UiState.Normal).data.isChangeNicknameBottomSheetVisible)
    }

    @Test
    fun onConfirmChangeNickname_withLeadingAt_stripsAtSign() = runTest(testDispatcher) {
        val user = User(
            uid = "user-123",
            username = "Tester",
            email = "test@example.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmChangeNickname("@hunter"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeProfileProvider.updateNicknameCalls)
        assertEquals("hunter", fakeProfileProvider.lastUpdateNickname)
    }

    @Test
    fun onConfirmChangeNickname_withMultipleWordsOrEmpty_doesNotUpdate() = runTest(testDispatcher) {
        val user = User(
            uid = "user-123",
            username = "Tester",
            email = "test@example.com",
            photoUrl = null,
            groups = emptyList(),
        )
        val fakeProfileProvider = FakeProfileProvider(userInfo = user)
        val viewModel = createViewModel(profileProvider = fakeProfileProvider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ProfileScreenAction.OnConfirmChangeNickname("two words"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, fakeProfileProvider.updateNicknameCalls)

        viewModel.onAction(ProfileScreenAction.OnConfirmChangeNickname("   "))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, fakeProfileProvider.updateNicknameCalls)
    }

    private fun createViewModel(
        profileProvider: ProfileProvider = FakeProfileProvider(),
        navigator: Navigator = FakeNavigator(),
        analyticsProvider: AnalyticsProvider = FakeAnalyticsProvider(),
    ): ProfileScreenViewModel {
        return ProfileScreenViewModel(
            navigator = navigator,
            profileProvider = profileProvider,
            authProvider = FakeAuthProvider(),
            analyticsProvider = analyticsProvider,
            logger = FakeAppLogger(),
        )
    }

    private class FakeProfileProvider(
        var userInfo: User? = null,
        var sections: List<UserGroupSection> = emptyList(),
    ) : ProfileProvider {
        var createNewUserGroupCalls = 0
        var lastCreatedGroupUserId: String? = null
        var lastCreatedGroupTitle: String? = null
        var lastInviteGroupId: String? = null
        var renameGroupCalls = 0
        var lastRenamedGroupId: String? = null
        var lastRenamedGroupTitle: String? = null
        var deleteGroupCalls = 0
        var lastDeletedGroupId: String? = null
        var leaveGroupCalls = 0
        var lastLeaveGroupUserId: String? = null
        var lastLeaveGroupId: String? = null

        override fun getUserInfoFlow(): Flow<User?> = flowOf(userInfo)

        override fun createNewInviteLink(inviteGroupId: String): String {
            lastInviteGroupId = inviteGroupId
            return "https://evgenysamarin.github.io/invite/$inviteGroupId"
        }

        override suspend fun joinGroup(userId: String, groupId: String): Boolean = true
        override suspend fun getGroupInfo(groupId: String): Group? = null

        override suspend fun createNewUserGroup(userId: String, title: String): String {
            createNewUserGroupCalls++
            lastCreatedGroupUserId = userId
            lastCreatedGroupTitle = title
            return "new-group-id"
        }

        override fun getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>> = flowOf(sections)

        override suspend fun renameGroup(groupId: String, newTitle: String): Boolean {
            renameGroupCalls++
            lastRenamedGroupId = groupId
            lastRenamedGroupTitle = newTitle
            return true
        }

        override suspend fun deleteGroup(groupId: String): Boolean {
            deleteGroupCalls++
            lastDeletedGroupId = groupId
            return true
        }

        override suspend fun leaveGroup(userId: String, groupId: String): Boolean {
            leaveGroupCalls++
            lastLeaveGroupUserId = userId
            lastLeaveGroupId = groupId
            return true
        }

        var updateNicknameCalls = 0
        var lastUpdateNicknameUserId: String? = null
        var lastUpdateNickname: String? = null

        override suspend fun updateNickname(userId: String, nickname: String): Boolean {
            updateNicknameCalls++
            lastUpdateNicknameUserId = userId
            lastUpdateNickname = nickname
            return true
        }
    }

    private class FakeNavigator : Navigator {
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        override suspend fun navigate(destination: Destination) {}
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
        override fun getCurrentUserIdFlow(): Flow<String?> = flowOf("test_uid")
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
