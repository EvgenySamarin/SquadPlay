package com.eysamarin.squadplay.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val navigator: Navigator,
) : ViewModel() {

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }
}
