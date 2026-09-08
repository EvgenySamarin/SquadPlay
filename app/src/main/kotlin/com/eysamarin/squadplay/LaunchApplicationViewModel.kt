package com.eysamarin.squadplay

import android.net.Uri
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.navigation.DeepLinkManager
import com.eysamarin.squadplay.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LaunchApplicationViewModel(
    private val authProvider: AuthProvider,
    private val deepLinkManager: DeepLinkManager,
) : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(true)

    val startDestination: StateFlow<Destination>
        field = MutableStateFlow<Destination>(Destination.AuthGraph)

    fun handleIncomingIntent(intentUri: Uri?) {
        viewModelScope.launch {
            val isUserExists = authProvider.isUserExists()
            val inviteGroupId = deepLinkManager.extractInviteGroupId(intentUri)

            if (inviteGroupId != null) {
                deepLinkManager.setPendingInviteGroupId(inviteGroupId)
            }

            if (isUserExists) {
                startDestination.value = Destination.HomeGraph
            } else {
                startDestination.value = Destination.AuthGraph
            }
            isLoading.value = false
        }
    }

    fun dismissPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            visiblePermissionDialogQueue.removeFirst()
        } else {
            if (visiblePermissionDialogQueue.isNotEmpty()) {
                visiblePermissionDialogQueue.removeAt(visiblePermissionDialogQueue.lastIndex)
            }
        }
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean,
    ) {
        if (!isGranted && visiblePermissionDialogQueue.contains(permission).not()) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}
