package com.eysamarin.squadplay.navigation

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DeepLinkManager {
    val pendingInviteGroupId: StateFlow<String?>
    fun setPendingInviteGroupId(groupId: String?)
    fun consumePendingInviteGroupId(): String?
    fun extractInviteGroupId(uri: Uri?): String?
}

class DefaultDeepLinkManager : DeepLinkManager {
    private val _pendingInviteGroupId = MutableStateFlow<String?>(null)
    override val pendingInviteGroupId: StateFlow<String?> = _pendingInviteGroupId.asStateFlow()

    override fun setPendingInviteGroupId(groupId: String?) {
        _pendingInviteGroupId.value = groupId
    }

    override fun consumePendingInviteGroupId(): String? {
        val current = _pendingInviteGroupId.value
        _pendingInviteGroupId.value = null
        return current
    }

    override fun extractInviteGroupId(uri: Uri?): String? {
        if (uri == null) return null
        val uriString = uri.toString()
        val regex = Regex("""^https://evgenysamarin\.github\.io/invite/([^/?#]+)""")
        return regex.find(uriString)?.groupValues?.get(1)
    }
}
