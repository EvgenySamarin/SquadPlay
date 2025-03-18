package com.eysamarin.squadplay.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.domain.calendar.CalendarUIProvider
import com.eysamarin.squadplay.domain.event.GameEventUIProvider
import com.eysamarin.squadplay.domain.polling.PollingProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.CalendarUI
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.NavAction
import com.eysamarin.squadplay.models.PollingDialogUI
import com.eysamarin.squadplay.models.Routes.Auth
import com.eysamarin.squadplay.models.Routes.Profile
import com.eysamarin.squadplay.models.TimeUnit
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class MainScreenViewModel(
    private val calendarUIProvider: CalendarUIProvider,
    private val gameEventUIProvider: GameEventUIProvider,
    private val pollingProvider: PollingProvider,
    private val authProvider: AuthProvider,
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<MainScreenUI>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val navigationChannel = Channel<NavAction>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    private val snackbarChannel = Channel<String>(Channel.RENDEZVOUS)
    val snackbarFlow = snackbarChannel.receiveAsFlow()

    private val _confirmInviteDialogState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val confirmInviteDialogState = _confirmInviteDialogState.asStateFlow()

    private val _inviteLinkState = MutableStateFlow<String?>(null)
    val inviteLinkState = _inviteLinkState.asStateFlow()

    private val _pollingDialogState = MutableStateFlow<UiState<PollingDialogUI>>(UiState.Empty)
    val pollingDialogState = _pollingDialogState.asStateFlow()

    init {
        viewModelScope.launch {
            val userInfo = profileProvider.getUserInfo()
            if (userInfo == null) {
                _uiState.emit(UiState.Error(description = "User info is null"))
                return@launch
            }

            val calendarState = calendarUIProvider.provideCalendarUIBy(yearMonth = YearMonth.now())
            updateMainScreenUI(MainScreenUI(
                user = userInfo,
                calendarUI = calendarState,
            ))
        }

        uiState
            .mapNotNull { it as? UiState.Normal<MainScreenUI> }
            .map { it.data.user }
            .combine(inviteLinkState.filterNotNull()) { user, inviteId ->
                user to inviteId
            }
            .onEach { (user, inviteId) ->
                if (user.inviteId == inviteId) {
                    snackbarChannel.send("You cannot invite yourself")
                    Log.w("TAG", "You cannot invite yourself")
                    return@onEach
                }

                val friendInfo = profileProvider.getFriendInfoByInviteId(inviteId)
                if (friendInfo == null) {
                    snackbarChannel.send("Friend with inviteId: $inviteId not found")
                    Log.w("TAG", "Friend with inviteId: $inviteId not found")
                    return@onEach
                }
                _confirmInviteDialogState.emit(UiState.Normal("Want to add ${friendInfo.username} as friend?"))
            }
            .launchIn(viewModelScope)
    }

    private fun updateMainScreenUI(updatedMainScreenUI: MainScreenUI) = viewModelScope.launch {
        Log.d("TAG", "updateMainScreenUI by: $updatedMainScreenUI")
        _uiState.emit(UiState.Normal(updatedMainScreenUI))
    }

    fun onLogOutTap() = viewModelScope.launch {
        Log.d("TAG", "onLogOutTap")
        val isSuccess = authProvider.signOut()
        if (isSuccess) {
            navigationChannel.send(NavAction.NavigateTo(Auth.route))
        } else {
            Log.d("TAG", "cannot log out")
        }
    }

    fun onAvatarTap() = viewModelScope.launch {
        Log.d("TAG", "onAvatarTap")
        navigationChannel.send(NavAction.NavigateTo(Profile.route))
    }

    fun onNextMonthTap(nextMonth: YearMonth) {
        Log.d("TAG", "onNextMonthTap: $nextMonth")

        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal<MainScreenUI>) return

        val nextMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = nextMonth)
        val newMainScreenUI = currentUiState.data.copy(
            calendarUI = nextMonthCalendarUI
        )
        updateMainScreenUI(newMainScreenUI)
    }

    fun onPreviousMonthTap(prevMonth: YearMonth) {
        Log.d("TAG", "onPreviousMonthTap: $prevMonth")

        val currentUiState = uiState.value
        if (currentUiState !is UiState.Normal<MainScreenUI>) return

        val prevMonthCalendarUI = calendarUIProvider.provideCalendarUIBy(yearMonth = prevMonth)
        val newMainScreenUI = currentUiState.data.copy(
            calendarUI = prevMonthCalendarUI
        )
        updateMainScreenUI(newMainScreenUI)
    }

    fun onDateTap(date: CalendarUI.Date) = viewModelScope.launch {
        Log.d("TAG", "onDateTap: $date, updating game events")

        val currentMainScreenUI = (uiState.value as? UiState.Normal<MainScreenUI>)?.data
        if (currentMainScreenUI == null) {
            Log.w("TAG", "current UI cannot be updated since of no data")
            return@launch
        }

        val updatedCalendarUI = calendarUIProvider.updateCalendarBySelectedDate(
            target = currentMainScreenUI.calendarUI,
            selectedDate = date,
        )

        val gameEvents = gameEventUIProvider.provideGameEventUIBy(date)
        updateMainScreenUI(
            updatedMainScreenUI = currentMainScreenUI.copy(
                calendarUI = updatedCalendarUI,
                gameEventsOnDate = gameEvents,
            )
        )
    }

    fun dismissPolingDialog() = viewModelScope.launch {
        Log.d("TAG", "dismissPolingDialog")

        _pollingDialogState.emit(UiState.Empty)
    }

    fun onPollingStartTap(timeFrom: TimeUnit, timeTo: TimeUnit) {
        val selectedDate = takeSelectedDate()

        Log.d("TAG", "onPollingStartTap for date: $selectedDate, timeFrom: $timeFrom, timeTo: $timeTo")
        pollingProvider.savePollingData("timeFrom: $timeFrom, timeTo: $timeTo")
    }

    fun onAddGameEventTap() = viewModelScope.launch {
        Log.d("TAG", "onAddGameEventTap show polling dialog state")

        val currentSelectedDate = takeSelectedDate()

        if (currentSelectedDate == null) {
            Log.w("TAG", "selected date is null cannot add game event")
            return@launch
        }

        _pollingDialogState.emit(UiState.Normal(PollingDialogUI(selectedDate = currentSelectedDate)))
    }

    private fun takeSelectedDate(): CalendarUI.Date? =
        (uiState.value as? UiState.Normal<MainScreenUI>)
            ?.data
            ?.calendarUI
            ?.dates
            ?.firstOrNull { it.isSelected }

    fun onInviteDeepLinkRetrieved(inviteId: String?) = viewModelScope.launch {
        if (inviteId == null) return@launch

        Log.d("TAG", "onInviteDeepLinkRetrieved: $inviteId")
        _inviteLinkState.emit(inviteId)
    }

    fun onAddFriendDialogConfirm() = viewModelScope.launch {
        Log.d("TAG", "onAddFriendDialogConfirm")

        val currentInviteId = inviteLinkState.value ?: run {
            Log.w("TAG", "currentInviteId is null, cannot add friend")
            return@launch
        }
        val currentUser = (uiState.value as? UiState.Normal<MainScreenUI>)?.data?.user ?: run {
            Log.w("TAG", "currentUser is null, cannot add friend")
            return@launch
        }

        val isSuccess = profileProvider.addFriend(
            userId = currentUser.uid, inviteId = currentInviteId,
        )
        snackbarChannel.send(
            if (isSuccess) {
                "Friend added successfully"
            } else {
                "Add friend failed"
            }
        )
    }

    fun onAddFriendDialogDismiss() = viewModelScope.launch {
        _confirmInviteDialogState.emit(UiState.Empty)
    }
}
