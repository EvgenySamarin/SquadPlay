package com.eysamarin.squadplay

import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eysamarin.squadplay.domain.auth.AuthProvider
import com.eysamarin.squadplay.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LaunchApplicationViewModel(
    private val authProvider: AuthProvider,
) : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<Destination>(Destination.AuthGraph)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            if (authProvider.isUserExists()) {
                _startDestination.value = Destination.HomeGraph
            }
            _isLoading.value = false
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
