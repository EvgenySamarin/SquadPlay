package com.eysamarin.squadplay

import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class LaunchApplicationViewModel : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            visiblePermissionDialogQueue.removeFirst()
        } else {
            visiblePermissionDialogQueue.removeAt(visiblePermissionDialogQueue. lastIndex)
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