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
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    private val userFriendsFlow = MutableStateFlow<List<Friend>>(emptyList())

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
            .map { it.groups }
            .filter { it.isNotEmpty() }
            .flatMapLatest { groups -> profileProvider.getGroupsMembersInfoFlow(groups) }
            .onEach {
                logger.d { "User friends received: $it" }
                userFriendsFlow.emit(it)
            }
            .launchIn(viewModelScope)


        combine(userInfoFlow, userFriendsFlow) { userInfo, friends ->
            userInfo?.let {
                userInfo to friends
            }
        }
            .filterNotNull()
            .onEach { (userInfo, friends) ->
                uiState.emit(UiState.Normal(ProfileScreenUI(user = userInfo, friends = friends)))
            }
            .launchIn(viewModelScope)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }

    fun onCreateInviteGroupLinkTap() = viewModelScope.launch {
        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal) return@launch

        val user = currentUiState.data.user
        val groupId = if (user.groups.isEmpty()) {
            val newGroupId = profileProvider.createNewUserGroup(user.uid)
            analyticsProvider.trackEvent(AnalyticsEvent.GroupCreated(newGroupId))
            newGroupId
        } else {
            //right now supported only one group
            user.groups.first().uid
        }

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
            ProfileScreenAction.OnCreateInviteLinkTap -> onCreateInviteGroupLinkTap()
            ProfileScreenAction.OnLogOutTap -> onLogOutTap()
            ProfileScreenAction.OnSettingsTap -> onSettingsTap()
        }
    }
}