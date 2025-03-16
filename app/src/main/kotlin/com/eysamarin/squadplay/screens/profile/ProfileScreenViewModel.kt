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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ProfileScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() = viewModelScope.launch {
        Log.d("TAG", "fetching user info")
        val userInfo = profileProvider.getUserInfo()

        if (userInfo == null) {
            _uiState.emit(UiState.Error(description = "User info is null"))
            return@launch
        }

        Log.d("TAG", "user info fetched: $userInfo")
        _uiState.emit(UiState.Normal(ProfileScreenUI(user = userInfo)))
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigationChannel.send(NavAction.NavigateBack)
    }

    fun onAddNewFriendTap() = viewModelScope.launch {
        Log.d("TAG", "onAddNewFriendTap")
    }
}