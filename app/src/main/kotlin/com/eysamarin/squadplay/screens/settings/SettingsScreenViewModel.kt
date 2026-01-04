package com.eysamarin.squadplay.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val navigator: Navigator,
) : ViewModel() {
    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }
}
