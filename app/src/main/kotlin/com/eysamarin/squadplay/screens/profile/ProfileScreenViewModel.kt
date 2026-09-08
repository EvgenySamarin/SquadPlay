package com.eysamarin.squadplay.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                Log.d("TAG", "user info received: $it")
                userInfoFlow.emit(it)
            }
            .map { it.groups }
            .filter { it.isNotEmpty() }
            .flatMapLatest { groups -> profileProvider.getGroupsMembersInfoFlow(groups) }
            .onEach {
                Log.d("TAG", "user friends received: $it")
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
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onCreateInviteGroupLinkTap() = viewModelScope.launch {
        Log.d("TAG", "onCreateInviteLinkTap")
        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal) return@launch

        val user = currentUiState.data.user
        val groupId = if (user.groups.isEmpty()) {
            profileProvider.createNewUserGroup(user.uid)
        } else {
            //right now supported only one group
            user.groups.first().uid
        }

        val inviteLink = profileProvider.createNewInviteLink(inviteGroupId = groupId)
        inviteLinkState.emit(UiState.Normal(inviteLink))
    }

    fun hideShareLink() = viewModelScope.launch {
        Log.d("TAG", "hideShareLink")
        inviteLinkState.emit(UiState.Empty)
    }

    fun onLogOutTap() = viewModelScope.launch {
        Log.d("TAG", "onLogOutTap")
        if (isLoggingOut.value) return@launch
        isLoggingOut.value = true
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            navigator.navigateToAuthGraph()
        } else {
            isLoggingOut.value = false
            Log.d("TAG", "cannot log out")
        }
    }

    fun onSettingsTap() = viewModelScope.launch {
        Log.d("TAG", "onSettingsTap")
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