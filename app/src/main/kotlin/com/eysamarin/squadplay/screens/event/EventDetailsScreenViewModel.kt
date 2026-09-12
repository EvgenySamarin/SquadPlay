package com.eysamarin.squadplay.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.event.EventProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.models.EventDetailsScreenAction
import com.eysamarin.squadplay.models.EventDetailsScreenUI
import com.eysamarin.squadplay.models.EventMemberUI
import com.eysamarin.squadplay.models.EventResponseStatus
import com.eysamarin.squadplay.navigation.Destination
import com.eysamarin.squadplay.navigation.Navigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailsScreenViewModel(
    private val navigator: Navigator,
    private val eventProvider: EventProvider,
    private val profileProvider: ProfileProvider? = null,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    private var currentUserId: String? = null
    private var membersJob: Job? = null

    private val _uiState = MutableStateFlow(
        EventDetailsScreenUI(
            eventId = "",
            title = "",
            date = "",
            imageUrl = null,
            isYourEvent = false,
            showDeleteConfirmation = false,
            userStatus = EventResponseStatus.NOT_SET,
            groupId = "",
            members = emptyList(),
        )
    )
    val uiState: StateFlow<EventDetailsScreenUI> = _uiState.asStateFlow()

    init {
        profileProvider?.let { provider ->
            viewModelScope.launch {
                provider.getUserInfoFlow()
                    .filterNotNull()
                    .collect { user ->
                        currentUserId = user.uid
                    }
            }
        }
    }

    fun initData(args: Destination.EventDetailsScreen) {
        _uiState.update {
            it.copy(
                eventId = args.eventId,
                title = args.title,
                date = args.date,
                imageUrl = args.imageUrl,
                isYourEvent = args.isYourEvent,
                userStatus = args.userStatus,
                groupId = args.groupId,
            )
        }
        loadGroupMembers(args.groupId, args.eventId)
    }

    fun onBackButtonTap() = viewModelScope.launch {
        navigator.navigateUp()
    }

    fun onDeleteTap() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onConfirmDeleteTap() = viewModelScope.launch {
        val eventId = _uiState.value.eventId
        _uiState.update { it.copy(showDeleteConfirmation = false) }
        val isSuccess = eventProvider.deleteEvent(eventId)
        if (isSuccess) {
            analyticsProvider.trackEvent(AnalyticsEvent.EventDeleted(eventId = eventId))
            navigator.navigateUp()
        } else {
            logger.w { "Failed to delete event $eventId" }
        }
    }

    fun updateEventResponse(status: EventResponseStatus) {
        if (_uiState.value.isYourEvent) return
        val eventId = _uiState.value.eventId
        val userId = currentUserId
        val newStatus = if (_uiState.value.userStatus == status) {
            EventResponseStatus.NOT_SET
        } else {
            status
        }
        _uiState.update { current ->
            val updatedMembers = if (userId != null) {
                current.members.map { member ->
                    if (member.uid == userId) member.copy(status = newStatus) else member
                }
            } else {
                current.members
            }
            current.copy(userStatus = newStatus, members = updatedMembers)
        }
        if (userId != null && eventId.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    eventProvider.updateEventResponse(eventId, userId, newStatus)
                } catch (e: Exception) {
                    logger.w { "Failed to update event response for $eventId: ${e.message}" }
                }
            }
        }
    }

    fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus) = viewModelScope.launch {
        _uiState.update { current ->
            val updatedMembers = current.members.map { member ->
                if (member.uid == userId) member.copy(status = status) else member
            }
            current.copy(userStatus = status, members = updatedMembers)
        }
        try {
            eventProvider.updateEventResponse(eventId, userId, status)
        } catch (e: Exception) {
            logger.w { "Failed to update event response for $eventId: ${e.message}" }
        }
    }

    private fun loadGroupMembers(groupId: String, eventId: String) {
        membersJob?.cancel()
        if (groupId.isBlank() || profileProvider == null) return

        membersJob = viewModelScope.launch {
            try {
                val group = profileProvider.getGroupInfo(groupId)
                if (group == null) {
                    logger.w { "Group $groupId not found for event $eventId" }
                    return@launch
                }

                val eventsFlow = eventProvider.getEventsFlow(setOf(groupId))
                val membersFlow = profileProvider.getGroupsMembersInfoFlow(listOf(group))

                combine(eventsFlow, membersFlow) { events, sections ->
                    val matchingEvent = events.firstOrNull { it.uid == eventId }
                    val friends = sections.firstOrNull { it.groupId == groupId }?.members
                        ?: sections.firstOrNull()?.members.orEmpty()

                    val membersList = friends.map { friend ->
                        val memberStatus = matchingEvent?.getStatusForUser(friend.uid) ?: EventResponseStatus.NOT_SET
                        EventMemberUI(
                            uid = friend.uid,
                            username = friend.username,
                            photoUrl = friend.photoUrl,
                            status = memberStatus,
                        )
                    }
                    val latestUserStatus = currentUserId?.let { uid ->
                        matchingEvent?.getStatusForUser(uid)
                    }
                    Pair(membersList, latestUserStatus)
                }.collect { (memberUIs, latestUserStatus) ->
                    _uiState.update { current ->
                        current.copy(
                            members = memberUIs,
                            userStatus = if (current.isYourEvent) current.userStatus else (latestUserStatus ?: current.userStatus)
                        )
                    }
                }
            } catch (e: Exception) {
                logger.w { "Failed to load members for group $groupId: ${e.message}" }
            }
        }
    }

    fun onAction(action: EventDetailsScreenAction) {
        when (action) {
            EventDetailsScreenAction.OnBackButtonTap -> onBackButtonTap()
            EventDetailsScreenAction.OnDeleteTap -> onDeleteTap()
            EventDetailsScreenAction.OnConfirmDeleteTap -> onConfirmDeleteTap()
            EventDetailsScreenAction.OnDismissDeleteDialog -> onDismissDeleteDialog()
            EventDetailsScreenAction.OnAcceptTap -> updateEventResponse(EventResponseStatus.ACCEPTED)
            EventDetailsScreenAction.OnRejectTap -> updateEventResponse(EventResponseStatus.REJECTED)
        }
    }
}
