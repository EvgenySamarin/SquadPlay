package com.eysamarin.squadplay.screens.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.models.EventDetailsScreenAction
import com.eysamarin.squadplay.models.EventDetailsScreenUI
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailsScreenViewModel(
    private val navigator: Navigator,
    private val eventProvider: EventProvider,
    private val analyticsProvider: AnalyticsProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EventDetailsScreenUI(
            eventId = "",
            title = "",
            date = "",
            imageUrl = null,
            isYourEvent = false,
            showDeleteConfirmation = false,
        )
    )
    val uiState: StateFlow<EventDetailsScreenUI> = _uiState.asStateFlow()

    fun initData(args: Destination.EventDetailsScreen) {
        _uiState.update {
            it.copy(
                eventId = args.eventId,
                title = args.title,
                date = args.date,
                imageUrl = args.imageUrl,
                isYourEvent = args.isYourEvent,
            )
        }
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onDeleteTap() {
        Log.d("TAG", "onDeleteTap")
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDismissDeleteDialog() {
        Log.d("TAG", "onDismissDeleteDialog")
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onConfirmDeleteTap() = viewModelScope.launch {
        val eventId = _uiState.value.eventId
        Log.d("TAG", "onConfirmDeleteTap: $eventId")
        _uiState.update { it.copy(showDeleteConfirmation = false) }
        val isSuccess = eventProvider.deleteEvent(eventId)
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.EventDeleted(eventId = eventId))
            navigator.navigateUp()
        } else {
            Log.w("TAG", "failed to delete event $eventId")
        }
    }

    fun onAction(action: EventDetailsScreenAction) {
        when (action) {
            EventDetailsScreenAction.OnBackButtonTap -> onBackButtonTap()
            EventDetailsScreenAction.OnDeleteTap -> onDeleteTap()
            EventDetailsScreenAction.OnConfirmDeleteTap -> onConfirmDeleteTap()
            EventDetailsScreenAction.OnDismissDeleteDialog -> onDismissDeleteDialog()
        }
    }
}
