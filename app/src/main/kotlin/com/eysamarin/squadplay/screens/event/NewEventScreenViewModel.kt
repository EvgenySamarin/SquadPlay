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
import com.eysamarin.squadplay.models.EventResponseStatus
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

    private val isSavingState = MutableStateFlow(false)
    private val cooldownRemainingSecondsState = MutableStateFlow(0L)
    private var cooldownJob: Job? = null

    internal var timeProvider: () -> Long = { System.currentTimeMillis() }

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
            .onEach { user ->
                logger.d { "User info received: $user" }
                userInfoState.emit(user)
                checkAndStartCooldown(user.lastEventCreatedAt)
                refreshUiState()
            }
            .launchIn(viewModelScope)

        navigationArgsState
            .filterNotNull()
            .onEach { args ->
                updateUiState(args)
            }
            .launchIn(viewModelScope)

        gameTitleState.onEach { title ->
            refreshUiState()
        }.launchIn(viewModelScope)

        gameThumbnailUrlState.onEach { url ->
            refreshUiState()
        }.launchIn(viewModelScope)
    }

    private fun checkAndStartCooldown(lastEventCreatedAt: Long?) {
        if (lastEventCreatedAt == null) return
        val now = timeProvider()
        val elapsed = now - lastEventCreatedAt
        if (elapsed in 0 until COOLDOWN_DURATION_MILLIS) {
            val remainingSeconds = ((COOLDOWN_DURATION_MILLIS - elapsed + 999L) / 1000L)
            startCooldownTimer(remainingSeconds)
        }
    }

    private fun startCooldownTimer(initialRemainingSeconds: Long) {
        cooldownJob?.cancel()
        if (initialRemainingSeconds <= 0L) {
            cooldownRemainingSecondsState.value = 0L
            refreshUiState()
            return
        }
        cooldownRemainingSecondsState.value = initialRemainingSeconds
        refreshUiState()
        cooldownJob = viewModelScope.launch {
            var remaining = initialRemainingSeconds
            while (remaining > 0L) {
                cooldownRemainingSecondsState.value = remaining
                refreshUiState()
                delay(1000.milliseconds)
                remaining--
            }
            cooldownRemainingSecondsState.value = 0L
            refreshUiState()
        }
    }

    private fun refreshUiState() {
        val args = navigationArgsState.value ?: return
        updateUiState(args)
    }

    private fun updateUiState(args: Destination.NewEventScreen) {
        val user = userInfoState.value
        val remaining = cooldownRemainingSecondsState.value
        uiState.value = UiState.Normal(
            NewEventScreenUI(
                title = "new event screen",
                selectedDate = args.selectedDate,
                yearMonth = LocalDate.parse(args.yearMonth),
                gameTitle = gameTitleState.value,
                eventIconUrl = gameThumbnailUrlState.value,
                userGroups = user?.groups.orEmpty(),
                isCooldownActive = remaining > 0L,
                cooldownRemainingSeconds = remaining,
                isSaving = isSavingState.value,
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
        if (isSavingState.value) {
            logger.d { "Save already in progress, ignoring duplicate tap" }
            return@launch
        }
        if (cooldownRemainingSecondsState.value > 0L) {
            logger.w { "Event creation is currently on cooldown" }
            return@launch
        }

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

        isSavingState.value = true
        refreshUiState()

        try {
            val eventData = Event(
                uid = UUID.randomUUID().toString(),
                creatorId = currentUser.uid,
                groupId = targetGroupId,
                title = title.takeIf { it.isNotBlank() } ?: "New event",
                eventIconUrl = eventIconUrl,
                fromDateTime = dateTimeFrom,
                toDateTime = dateTimeTo,
                responses = mapOf(currentUser.uid to EventResponseStatus.ACCEPTED.name),
            )
            val isSuccess = eventProvider.saveEventData(eventData)
            if (isSuccess) {
                startCooldownTimer(COOLDOWN_DURATION_SECONDS)
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
        } finally {
            isSavingState.value = false
            refreshUiState()
        }
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

    companion object {
        const val COOLDOWN_DURATION_SECONDS = 30L
        const val COOLDOWN_DURATION_MILLIS = COOLDOWN_DURATION_SECONDS * 1000L
    }
}
