package com.eysamarin.squadplay.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.ProfileScreenAction
import com.eysamarin.squadplay.models.ProfileScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.models.UserGroupSection
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val navigator: Navigator,
    private val profileProvider: ProfileProvider,
    private val authProvider: AuthProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {
    val uiState: StateFlow<UiState<ProfileScreenUI>>
        field = MutableStateFlow<UiState<ProfileScreenUI>>(UiState.Loading)

    val isLoggingOut: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)

    val inviteLinkState: StateFlow<UiState<String>>
        field = MutableStateFlow<UiState<String>>(UiState.Empty)

    private val userInfoFlow = MutableStateFlow<User?>(null)
    private val userGroupsFlow = MutableStateFlow<List<UserGroupSection>>(emptyList())
    private val isCreateGroupBottomSheetVisibleFlow = MutableStateFlow(false)

    init {
        collectUserInfo()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectUserInfo() {
        profileProvider.getUserInfoFlow()
            .onEach {
                if (it == null) {
                    navigator.navigateToAuthGraph()
                }
            }
            .filterNotNull()
            .onEach {
                logger.d { "User info received: $it" }
                userInfoFlow.emit(it)
            }
            .flatMapLatest { user ->
                if (user.groups.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    profileProvider.getGroupsMembersInfoFlow(user.groups)
                }
            }
            .onEach {
                logger.d { "User group sections received: $it" }
                userGroupsFlow.emit(it)
            }
            .launchIn(viewModelScope)

        combine(
            userInfoFlow,
            userGroupsFlow,
            isCreateGroupBottomSheetVisibleFlow
        ) { userInfo, groupSections, isBottomSheetVisible ->
            userInfo?.let {
                Triple(userInfo, groupSections, isBottomSheetVisible)
            }
        }
            .filterNotNull()
            .onEach { (userInfo, groupSections, isBottomSheetVisible) ->
                uiState.emit(
                    UiState.Normal(
                        ProfileScreenUI(
                            user = userInfo,
                            groupSections = groupSections,
                            isCreateGroupBottomSheetVisible = isBottomSheetVisible,
                        )
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }

    fun onCreateInviteGroupLinkTap(groupId: String) = viewModelScope.launch {
        analyticsProvider.trackEvent(AnalyticsEvent.ShareInviteClicked(groupId))
        val inviteLink = profileProvider.createNewInviteLink(inviteGroupId = groupId)
        inviteLinkState.emit(UiState.Normal(inviteLink))
    }

    fun onCreateNewGroupTap() {
        isCreateGroupBottomSheetVisibleFlow.value = true
    }

    fun onDismissCreateGroupBottomSheet() {
        isCreateGroupBottomSheetVisibleFlow.value = false
    }

    fun onConfirmCreateGroup(title: String) = viewModelScope.launch {
        isCreateGroupBottomSheetVisibleFlow.value = false
        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal) return@launch
        val userId = currentUiState.data.user.uid
        val newGroupId = profileProvider.createNewUserGroup(userId, title)
        analyticsProvider.trackEvent(AnalyticsEvent.GroupCreated(newGroupId))
    }

    fun hideShareLink() = viewModelScope.launch {
        inviteLinkState.emit(UiState.Empty)
    }

    fun onLogOutTap() = viewModelScope.launch {
        if (isLoggingOut.value) return@launch
        isLoggingOut.value = true
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.SignOut)
            navigator.navigateToAuthGraph()
        } else {
            isLoggingOut.value = false
            logger.w { "Failed to sign out" }
        }
    }

    fun onSettingsTap() = viewModelScope.launch {
        navigator.navigate(Destination.SettingsScreen)
    }

    fun onEditGroupTap(groupId: String) {
        logger.d { "onEditGroupTap: groupId=$groupId" }
        analyticsProvider.trackEvent(AnalyticsEvent.EditGroupClicked(groupId))
    }

    fun onDeleteGroupTap(groupId: String) {
        logger.d { "onDeleteGroupTap: groupId=$groupId" }
        analyticsProvider.trackEvent(AnalyticsEvent.DeleteGroupClicked(groupId))
    }

    fun onLeaveGroupTap(groupId: String) {
        logger.d { "onLeaveGroupTap: groupId=$groupId" }
        analyticsProvider.trackEvent(AnalyticsEvent.LeaveGroupClicked(groupId))
    }

    fun onConfirmEditGroup(groupId: String, newTitle: String) = viewModelScope.launch {
        val trimmed = newTitle.trim()
        val words = if (trimmed.isEmpty()) emptyList() else trimmed.split("\\s+".toRegex())
        if (words.size != 1) return@launch

        val isSuccess = profileProvider.renameGroup(groupId = groupId, newTitle = trimmed)
        if (isSuccess) {
            logger.d { "Group renamed successfully: $groupId to $trimmed" }
            analyticsProvider.trackEvent(AnalyticsEvent.GroupRenamed(groupId))
        } else {
            logger.w { "Failed to rename group $groupId" }
        }
    }

    fun onConfirmDeleteGroup(groupId: String) = viewModelScope.launch {
        val isSuccess = profileProvider.deleteGroup(groupId = groupId)
        if (isSuccess) {
            logger.d { "Group deleted successfully: $groupId" }
            analyticsProvider.trackEvent(AnalyticsEvent.GroupDeleted(groupId))
        } else {
            logger.w { "Failed to delete group $groupId" }
        }
    }

    fun onConfirmLeaveGroup(groupId: String) = viewModelScope.launch {
        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal) return@launch
        val userId = currentUiState.data.user.uid
        val isSuccess = profileProvider.leaveGroup(userId = userId, groupId = groupId)
        if (isSuccess) {
            logger.d { "Group left successfully: $groupId for user $userId" }
            analyticsProvider.trackEvent(AnalyticsEvent.GroupLeft(groupId))
        } else {
            logger.w { "Failed to leave group $groupId for user $userId" }
        }
    }

    fun onAction(action: ProfileScreenAction) {
        when (action) {
            ProfileScreenAction.OnBackButtonTap -> onBackButtonTap()
            is ProfileScreenAction.OnCreateInviteLinkTap -> onCreateInviteGroupLinkTap(action.groupId)
            ProfileScreenAction.OnCreateNewGroupTap -> onCreateNewGroupTap()
            ProfileScreenAction.OnDismissCreateGroupBottomSheet -> onDismissCreateGroupBottomSheet()
            is ProfileScreenAction.OnConfirmCreateGroup -> onConfirmCreateGroup(action.title)
            ProfileScreenAction.OnLogOutTap -> onLogOutTap()
            ProfileScreenAction.OnSettingsTap -> onSettingsTap()
            is ProfileScreenAction.OnDeleteGroupTap -> onDeleteGroupTap(action.groupId)
            is ProfileScreenAction.OnEditGroupTap -> onEditGroupTap(action.groupId)
            is ProfileScreenAction.OnLeaveGroupTap -> onLeaveGroupTap(action.groupId)
            is ProfileScreenAction.OnConfirmEditGroup -> onConfirmEditGroup(action.groupId, action.newTitle)
            is ProfileScreenAction.OnConfirmDeleteGroup -> onConfirmDeleteGroup(action.groupId)
            is ProfileScreenAction.OnConfirmLeaveGroup -> onConfirmLeaveGroup(action.groupId)
        }
    }
}