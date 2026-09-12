package com.eysamarin.squadplay.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.contracts.AnalyticsEvent
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.domain.analytics.AnalyticsProvider
import com.eysamarin.squadplay.domain.profile.ProfileProvider
import com.eysamarin.squadplay.domain.resource.StringProvider
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.eysamarin.squadplay.models.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeGraphViewModel(
    private val deepLinkManager: DeepLinkManager,
    private val profileProvider: ProfileProvider,
    private val snackbar: SnackbarProvider,
    private val stringProvider: StringProvider,
    private val analyticsProvider: AnalyticsProvider,
    private val logger: AppLogger,
) : ViewModel() {

    private val _confirmInviteDialogState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val confirmInviteDialogState: StateFlow<UiState<String>> = _confirmInviteDialogState.asStateFlow()

    private var activeInviteGroupId: String? = null

    init {
        observePendingDeepLinks()
    }

    private fun observePendingDeepLinks() {
        viewModelScope.launch {
            deepLinkManager.pendingInviteGroupId
                .filterNotNull()
                .collect { inviteGroupId ->
                    deepLinkManager.consumePendingInviteGroupId()
                    handleInviteGroupId(inviteGroupId)
                }
        }
    }

    private suspend fun handleInviteGroupId(inviteGroupId: String) {
        logger.d(tag = TAG) { "Handling invite group deep link: $inviteGroupId" }
        val currentUser = profileProvider.getUserInfoFlow().filterNotNull().firstOrNull()
        if (currentUser == null) {
            logger.w(tag = TAG) { "Current user is null, cannot process invite deep link" }
            return
        }

        if (currentUser.groups.any { it.uid == inviteGroupId }) {
            snackbar.showMessage(stringProvider.alreadyInSquad)
            logger.w(tag = TAG) { "User already in squad $inviteGroupId" }
            return
        }

        val groupInfo = profileProvider.getGroupInfo(inviteGroupId)
        if (groupInfo == null) {
            snackbar.showMessage(stringProvider.squadNotFound(inviteGroupId))
            logger.w(tag = TAG) { "Group with id $inviteGroupId not found" }
            return
        }

        activeInviteGroupId = inviteGroupId
        _confirmInviteDialogState.value = UiState.Normal(stringProvider.wantToJoinSquad(groupInfo.title))
    }

    fun onJoinGroupDialogConfirm() {
        val groupId = activeInviteGroupId ?: run {
            logger.w(tag = TAG) { "activeInviteGroupId is null, cannot join" }
            return
        }
        activeInviteGroupId = null
        _confirmInviteDialogState.value = UiState.Empty

        viewModelScope.launch {
            val currentUser = profileProvider.getUserInfoFlow().filterNotNull().firstOrNull()
            if (currentUser == null) {
                logger.w(tag = TAG) { "Current user is null on confirm, cannot join" }
                return@launch
            }
            val isSuccess = profileProvider.joinGroup(userId = currentUser.uid, groupId = groupId)
            if (isSuccess) {
                analyticsProvider.trackEvent(AnalyticsEvent.JoinGroup(groupId))
            } else {
                logger.w(tag = TAG) { "Failed to join group $groupId" }
            }
            snackbar.showMessage(
                if (isSuccess) stringProvider.joinedSquad else stringProvider.joinSquadFailed
            )
        }
    }

    fun onJoinGroupDialogDismiss() {
        activeInviteGroupId = null
        _confirmInviteDialogState.value = UiState.Empty
    }

    companion object {
        private const val TAG = "HomeGraph"
    }
}
