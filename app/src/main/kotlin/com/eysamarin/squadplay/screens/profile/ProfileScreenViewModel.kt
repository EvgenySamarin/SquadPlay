package com.eysamarin.squadplay.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.Friend
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.ProfileScreenUI
import com.eysamarin.squadplay.models.Route.Auth
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val profileProvider: ProfileProvider,
    private val authProvider: AuthProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ProfileScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _inviteLinkState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val inviteLinkState = _inviteLinkState.asStateFlow()


    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    private val userInfoFlow = MutableStateFlow<User?>(null)
    private val userFriendsFlow = MutableStateFlow<List<Friend>>(emptyList())

    init {
        collectUserInfo()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectUserInfo() {
        profileProvider.getUserInfoFlow()
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
                _uiState.emit(UiState.Normal(ProfileScreenUI(user = userInfo, friends = friends)))
            }
            .launchIn(viewModelScope)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigationChannel.send(NavAction.NavigateBack)
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
        _inviteLinkState.emit(UiState.Normal(inviteLink))
    }

    fun hideShareLink() = viewModelScope.launch {
        Log.d("TAG", "hideShareLink")
        _inviteLinkState.emit(UiState.Empty)
    }

    fun onLogOutTap() = viewModelScope.launch {
        Log.d("TAG", "onLogOutTap")
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            navigationChannel.send(NavAction.NavigateTo(Auth.route))
        } else {
            Log.d("TAG", "cannot log out")
        }
    }
}