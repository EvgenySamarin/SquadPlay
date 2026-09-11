package com.eysamarin.squadplay.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.Friend
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

        combine(userInfoFlow, userGroupsFlow) { userInfo, groupSections ->
            userInfo?.let {
                userInfo to groupSections
            }
        }
            .filterNotNull()
            .onEach { (userInfo, groupSections) ->
                uiState.emit(UiState.Normal(ProfileScreenUI(user = userInfo, groupSections = groupSections)))
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

    fun onAction(action: ProfileScreenAction) {
        when (action) {
            ProfileScreenAction.OnBackButtonTap -> onBackButtonTap()
            is ProfileScreenAction.OnCreateInviteLinkTap -> onCreateInviteGroupLinkTap(action.groupId)
            ProfileScreenAction.OnLogOutTap -> onLogOutTap()
            ProfileScreenAction.OnSettingsTap -> onSettingsTap()
        }
    }
}