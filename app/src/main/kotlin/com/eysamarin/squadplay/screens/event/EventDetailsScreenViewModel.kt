package com.eysamarin.squadplay.screens.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.models.EventDetailsScreenAction
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.launch

class EventDetailsScreenViewModel(
    private val navigator: Navigator,
) : ViewModel() {

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onAction(action: EventDetailsScreenAction) {
        when (action) {
            EventDetailsScreenAction.OnBackButtonTap -> onBackButtonTap()
        }
    }
}
