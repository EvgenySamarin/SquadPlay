package com.eysamarin.squadplay.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.ProfileScreenUI
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ProfileScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _inviteLinkState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val inviteLinkState = _inviteLinkState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    init {
        collectUserInfo()
    }

    private fun collectUserInfo() {
        Log.d("TAG", "subscribe on user info flow")
        profileProvider.getUserInfoFlow()
            .filterNotNull()
            .onEach { userInfo ->
                Log.d("TAG", "user info received: $userInfo")
                _uiState.emit(UiState.Normal(ProfileScreenUI(user = userInfo)))
            }
            .launchIn(viewModelScope)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigationChannel.send(NavAction.NavigateBack)
    }

    fun onCreateInviteLinkTap() = viewModelScope.launch {
        Log.d("TAG", "onCreateInviteLinkTap")
        val currentUiState = uiState.value
        if(currentUiState !is UiState.Normal) return@launch

        val inviteLink = profileProvider.createNewInviteLink(currentUiState.data.user.inviteId)
        _inviteLinkState.emit(UiState.Normal(inviteLink))
    }

    fun hideShareLink() = viewModelScope.launch {
        Log.d("TAG", "hideShareLink")
        _inviteLinkState.emit(UiState.Empty)
    }
}