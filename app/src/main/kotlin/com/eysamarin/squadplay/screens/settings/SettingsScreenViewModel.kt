package com.eysamarin.squadplay.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val navigator: Navigator,
) : ViewModel() {

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }
}
