package com.eysamarin.squadplay.screens.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.NewEventScreenAction
import com.eysamarin.squadplay.models.NewEventScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.util.UUID


import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.game.GameProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class NewEventScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val profileProvider: ProfileProvider,
    private val eventProvider: EventProvider,
    private val stringProvider: StringProvider,
    private val gameProvider: GameProvider,
    private val analyticsProvider: AnalyticsProvider,
) : ViewModel() {
    val uiState: StateFlow<UiState<NewEventScreenUI>>
        field = MutableStateFlow<UiState<NewEventScreenUI>>(UiState.Loading)

    private val userInfoState = MutableStateFlow<User?>(null)
    private val navigationArgsState = MutableStateFlow<Destination.NewEventScreen?>(null)
    
    private val gameTitleState = MutableStateFlow("")
    private val gameThumbnailUrlState = MutableStateFlow<String?>(null)
    private var searchJob: Job? = null

    init {
        collectInitScreenData()
    }

    fun updateSelectedDate(args: Destination.NewEventScreen) = viewModelScope.launch {
        navigationArgsState.value = args
    }

    private fun collectInitScreenData() {
        profileProvider.getUserInfoFlow()
            .onEach {
                if (it == null) {
                    navigator.navigate(Destination.AuthScreen)
                }
            }
            .filterNotNull()
            .onEach {
                Log.d("TAG", "user info received: $it")
                userInfoState.emit(it)
            }
            .launchIn(viewModelScope)

        navigationArgsState
            .filterNotNull()
            .onEach { args ->
                updateUiState(args)
            }
            .launchIn(viewModelScope)
            
        gameTitleState.onEach { title ->
            val args = navigationArgsState.value ?: return@onEach
            updateUiState(args)
        }.launchIn(viewModelScope)
        
        gameThumbnailUrlState.onEach { url ->
            val args = navigationArgsState.value ?: return@onEach
            updateUiState(args)
        }.launchIn(viewModelScope)
    }
    
    private fun updateUiState(args: Destination.NewEventScreen) {
        uiState.value = UiState.Normal(
            NewEventScreenUI(
                title = "new event screen",
                selectedDate = args.selectedDate,
                yearMonth = LocalDate.parse(args.yearMonth),
                gameTitle = gameTitleState.value,
                eventIconUrl = gameThumbnailUrlState.value,
            )
        )
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onEventSaveTap(title: String, dateTimeFrom: LocalDateTime, dateTimeTo: LocalDateTime, eventIconUrl: String?) = viewModelScope.launch {
        Log.d("TAG", "onEventSaveTap for dates: $dateTimeFrom - $dateTimeTo")
        val currentUser = userInfoState.value ?: run {
            Log.w("TAG", "currentUser is null cannot save event")
            return@launch
        }

        if (currentUser.groups.isEmpty()) {
            Log.d("TAG", "currentUser has no groups cannot save event")
            snackbar.showMessage(stringProvider.youHaveNoSquad)
            return@launch
        }

        val eventData = Event(
            uid = UUID.randomUUID().toString(),
            creatorId = currentUser.uid,
            groupId = currentUser.groups.first().uid,
            title = title.takeIf { it.isNotBlank() } ?: "New event",
            eventIconUrl = eventIconUrl,
            fromDateTime = dateTimeFrom,
            toDateTime = dateTimeTo,
        )
        val isSuccess = eventProvider.saveEventData(eventData)
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.EventSaved(eventId = eventData.uid))
            navigator.navigateUp()
        }
        snackbar.showMessage(
            if (isSuccess) {
                stringProvider.eventSaved
            } else {
                stringProvider.eventSaveFailed
            }
        )
    }
    
    fun onGameTitleChanged(title: String) {
        gameTitleState.value = title
        searchJob?.cancel()
        if (title.isBlank()) {
            gameThumbnailUrlState.value = null
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            val url = gameProvider.getGameThumbnailUrl(title)
            gameThumbnailUrlState.value = url
        }
    }

    fun onAction(action: NewEventScreenAction) {
        when (action) {
            NewEventScreenAction.OnBackButtonTap -> onBackButtonTap()
            is NewEventScreenAction.OnEventSaveTap -> onEventSaveTap(
                title = action.title,
                dateTimeFrom = action.timeFrom,
                dateTimeTo = action.timeTo,
                eventIconUrl = action.eventIconUrl,
            )
            is NewEventScreenAction.OnGameTitleChanged -> onGameTitleChanged(action.title)
        }
    }
}
