package com.eysamarin.squadplay.screens.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.models.Event
import com.eysamarin.squadplay.models.NewEventScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID


class NewEventScreenViewModel(
    private val navigator: Navigator,
    private val profileProvider: ProfileProvider,
    private val eventProvider: EventProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<NewEventScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    private val userInfoState = MutableStateFlow<User?>(null)
    private val navigationArgsState = MutableStateFlow<Destination.NewEventScreen?>(null)

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
                _uiState.value = UiState.Normal(
                    NewEventScreenUI(
                        title = "new event screen",
                        selectedDate = args.selectedDate,
                        yearMonth = YearMonth.parse(args.yearMonth),
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        Log.d("TAG", "onBackButtonTap")
        navigator.navigateUp()
    }

    fun onEventSaveTap(dateTimeFrom: LocalDateTime, dateTimeTo: LocalDateTime) = viewModelScope.launch {
        Log.d("TAG", "onEventSaveTap for dates: $dateTimeFrom - $dateTimeTo")
        val currentUser = userInfoState.value ?: run {
            Log.w("TAG", "currentUser is null cannot save event")
            return@launch
        }

        if (currentUser.groups.isEmpty()) {
            Log.d("TAG", "currentUser has no groups cannot save event")
            snackbarChannel.send(stringProvider.youHaveNoSquad)
            return@launch
        }

        val eventData = Event(
            uid = UUID.randomUUID().toString(),
            creatorId = currentUser.uid,
            groupId = currentUser.groups.first().uid,
            title = "New event",
            fromDateTime = dateTimeFrom,
            toDateTime = dateTimeTo,
        )
        val isSuccess = eventProvider.saveEventData(eventData)
        snackbarChannel.send(
            if (isSuccess) {
                stringProvider.eventSaved
            } else {
                stringProvider.eventSaveFailed
            }
        )
    }
}