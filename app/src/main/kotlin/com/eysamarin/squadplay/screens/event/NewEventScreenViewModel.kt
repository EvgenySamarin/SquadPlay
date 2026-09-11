package com.eysamarin.squadplay.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.game.GameProvider
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class NewEventScreenViewModel(
    private val navigator: Navigator,
    private val snackbar: SnackbarProvider,
    private val profileProvider: ProfileProvider,
    private val eventProvider: EventProvider,
    private val stringProvider: StringProvider,
    private val gameProvider: GameProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
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
                logger.d { "User info received: $it" }
                userInfoState.emit(it)
                val args = navigationArgsState.value ?: return@onEach
                updateUiState(args)
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
        val user = userInfoState.value
        uiState.value = UiState.Normal(
            NewEventScreenUI(
                title = "new event screen",
                selectedDate = args.selectedDate,
                yearMonth = LocalDate.parse(args.yearMonth),
                gameTitle = gameTitleState.value,
                eventIconUrl = gameThumbnailUrlState.value,
                userGroups = user?.groups.orEmpty(),
            )
        )
    }

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }

    fun onEventSaveTap(
        title: String,
        dateTimeFrom: LocalDateTime,
        dateTimeTo: LocalDateTime,
        eventIconUrl: String?,
        groupId: String,
    ) = viewModelScope.launch {
        val currentUser = userInfoState.value ?: run {
            logger.w { "Current user is null, cannot save event" }
            return@launch
        }

        if (currentUser.groups.isEmpty()) {
            logger.w { "Current user has no groups, cannot save event" }
            snackbar.showMessage(stringProvider.youHaveNoSquad)
            return@launch
        }

        val targetGroupId = groupId.ifBlank {
            currentUser.groups.firstOrNull()?.uid ?: run {
                snackbar.showMessage(stringProvider.youHaveNoSquad)
                return@launch
            }
        }

        val eventData = Event(
            uid = UUID.randomUUID().toString(),
            creatorId = currentUser.uid,
            groupId = targetGroupId,
            title = title.takeIf { it.isNotBlank() } ?: "New event",
            eventIconUrl = eventIconUrl,
            fromDateTime = dateTimeFrom,
            toDateTime = dateTimeTo,
        )
        val isSuccess = eventProvider.saveEventData(eventData)
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.EventSaved(eventId = eventData.uid))
            navigator.navigateUp()
        } else {
            logger.w { "Failed to save event data for user ${currentUser.uid}" }
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
            delay(500.milliseconds) // Debounce
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
                groupId = action.groupId,
            )
            is NewEventScreenAction.OnGameTitleChanged -> onGameTitleChanged(action.title)
        }
    }
}
